package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundEngine(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile var sfxEnabled: Boolean = true
    @Volatile var musicEnabled: Boolean = true
    @Volatile var vibrationEnabled: Boolean = true
    @Volatile private var isForeground: Boolean = true

    private var musicJob: Job? = null

    private val sampleRate = 22050

    // Pre-synthesized crisp PCM buffers
    private val clickPcm by lazy { synthTone(freqs = listOf(880.0), durationMs = 45, decay = 18.0, volume = 0.25) }
    private val selectPcm by lazy { synthTone(freqs = listOf(660.0, 990.0), durationMs = 60, decay = 14.0, volume = 0.30) }
    private val escapePcm by lazy { synthGlide(startHz = 520.0, endHz = 1180.0, durationMs = 170, volume = 0.35) }
    private val blockedPcm by lazy { synthTone(freqs = listOf(180.0, 130.0), durationMs = 130, decay = 11.0, volume = 0.40) }
    private val rewardPcm by lazy { synthSequence(listOf(784.0 to 90, 1174.66 to 180), volume = 0.32) }
    private val winPcm by lazy { synthSequence(listOf(523.25 to 90, 659.25 to 90, 783.99 to 90, 1046.5 to 260), volume = 0.35) }
    private val gameOverPcm by lazy { synthSequence(listOf(392.0 to 150, 349.23 to 150, 293.66 to 300), volume = 0.32) }

    fun updateSettings(sfx: Boolean, music: Boolean, vibration: Boolean) {
        sfxEnabled = sfx
        vibrationEnabled = vibration
        if (musicEnabled != music) {
            musicEnabled = music
            syncMusicPlayback()
        }
    }

    fun setAppForeground(foreground: Boolean) {
        isForeground = foreground
        syncMusicPlayback()
    }

    fun playButtonClick() {
        if (sfxEnabled) playPcmAsync(clickPcm)
        vibrateShort(10L)
    }

    fun playArrowSelect() {
        if (sfxEnabled) playPcmAsync(selectPcm)
        vibrateShort(12L)
    }

    fun playArrowEscape() {
        if (sfxEnabled) playPcmAsync(escapePcm)
        vibrateShort(18L)
    }

    fun playBlockedBump() {
        if (sfxEnabled) playPcmAsync(blockedPcm)
        vibrateError()
    }

    fun playReward() {
        if (sfxEnabled) playPcmAsync(rewardPcm)
        vibrateShort(25L)
    }

    fun playLevelComplete() {
        if (sfxEnabled) playPcmAsync(winPcm)
        vibrateSuccess()
    }

    fun playGameOver() {
        if (sfxEnabled) playPcmAsync(gameOverPcm)
        vibrateError()
    }

    private fun syncMusicPlayback() {
        if (musicEnabled && isForeground) {
            if (musicJob?.isActive == true) return
            musicJob = scope.launch {
                // Soft minimalist ambient chord pad loop (Cadd9 -> Am7 -> Fmaj7 -> G6)
                val chords = listOf(
                    listOf(261.63, 329.63, 392.00, 587.33),
                    listOf(220.00, 261.63, 329.63, 392.00),
                    listOf(174.61, 220.00, 261.63, 329.63),
                    listOf(196.00, 246.94, 293.66, 329.63)
                )
                var idx = 0
                while (isActive && musicEnabled && isForeground) {
                    val chord = chords[idx % chords.size]
                    idx++
                    val pad = synthPadChord(chord, durationMs = 2400, volume = 0.06)
                    playPcmBlocking(pad)
                    delay(120)
                }
            }
        } else {
            musicJob?.cancel()
            musicJob = null
        }
    }

    private fun playPcmAsync(pcm: ShortArray) {
        scope.launch { playPcmBlocking(pcm) }
    }

    private fun playPcmBlocking(pcm: ShortArray) {
        try {
            val byteSize = pcm.size * 2
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) return

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(maxOf(byteSize, minBuf))
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            val durationMs = (pcm.size * 1000L) / sampleRate
            Thread.sleep(durationMs + 25L)
            track.stop()
            track.release()
        } catch (_: Throwable) {
            // Safe no-op on headless/JVM test environments
        }
    }

    private fun synthTone(
        freqs: List<Double>,
        durationMs: Int,
        decay: Double,
        volume: Double
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val env = exp(-decay * t) * (1.0 - exp(-200.0 * t))
            var sum = 0.0
            for (f in freqs) {
                sum += sin(2.0 * PI * f * t)
            }
            val sample = (sum / freqs.size) * env * volume * Short.MAX_VALUE
            out[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun synthGlide(
        startHz: Double,
        endHz: Double,
        durationMs: Int,
        volume: Double
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val frac = i.toDouble() / count
            val freq = startHz + (endHz - startHz) * frac
            val env = sin(PI * frac)
            phase += 2.0 * PI * freq / sampleRate
            val sample = sin(phase) * env * volume * Short.MAX_VALUE
            out[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun synthSequence(notes: List<Pair<Double, Int>>, volume: Double): ShortArray {
        val segments = notes.map { (freq, durMs) ->
            synthTone(listOf(freq, freq * 1.5), durMs, decay = 6.5, volume = volume)
        }
        val total = segments.sumOf { it.size }
        val out = ShortArray(total)
        var offset = 0
        for (seg in segments) {
            seg.copyInto(out, offset)
            offset += seg.size
        }
        return out
    }

    private fun synthPadChord(freqs: List<Double>, durationMs: Int, volume: Double): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val frac = i.toDouble() / count
            val env = sin(PI * frac)
            var sum = 0.0
            for (f in freqs) {
                sum += sin(2.0 * PI * f * t)
            }
            val sample = (sum / freqs.size) * env * volume * Short.MAX_VALUE
            out[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun getVibrator(): Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Throwable) {
        null
    }

    private fun vibrateShort(millis: Long) {
        if (!vibrationEnabled) return
        try {
            val v = getVibrator() ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(millis)
            }
        } catch (_: Throwable) {
        }
    }

    private fun vibrateError() {
        if (!vibrationEnabled) return
        try {
            val v = getVibrator() ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 35, 45), -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(80L)
            }
        } catch (_: Throwable) {
        }
    }

    private fun vibrateSuccess() {
        if (!vibrationEnabled) return
        try {
            val v = getVibrator() ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 35), -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(60L)
            }
        } catch (_: Throwable) {
        }
    }
}
