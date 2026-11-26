package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DailyChallengeEntity
import com.example.domain.CalendarDayInfo
import com.example.domain.DateHelper
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceSurface
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBorderBlue

@Composable
fun DailyChallengeScreen(
    dailyCompletions: Map<Int, DailyChallengeEntity>,
    onPlayDay: (epochDay: Long, dayOfMonth: Int) -> Unit,
    onBack: () -> Unit
) {
    val monthTitle = remember { DateHelper.currentMonthTitle() }
    val days = remember { DateHelper.currentMonthDays() }
    val firstDayOffset = remember { DateHelper.firstDayOfMonthOffset() }
    val todayInfo = remember(days) { days.firstOrNull { it.isToday } ?: days.first() }

    val completedCount = dailyCompletions.size
    val monthlyStars = dailyCompletions.values.sumOf { it.stars }
    val monthlyTrophies = completedCount / 5
    val progressFrac = (completedCount / days.size.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IceSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("daily_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = CobaltPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Challenge",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CobaltDark
                    )
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monthly Progress & Trophy Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_trophy_sapphire_gold),
                                contentDescription = "Monthly Trophy",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$completedCount / ${days.size} Days Completed",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CobaltDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            " $monthlyStars Stars",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.EmojiEvents,
                                            contentDescription = null,
                                            tint = CobaltPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            " $monthlyTrophies Trophies",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progressFrac },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50)),
                            color = CobaltPrimary,
                            trackColor = IceBlueContainer
                        )

                        Text(
                            text = "Every 5 completed daily puzzles awards a Monthly Trophy + Bonus Hints. Tap any unlocked day to play or revisit!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { onPlayDay(todayInfo.epochDay, todayInfo.dayOfMonth) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("play_today_daily_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (dailyCompletions.containsKey(todayInfo.dayOfMonth)) {
                                    "Replay Today's Challenge (Day ${todayInfo.dayOfMonth})"
                                } else {
                                    "Play Today's Challenge (Day ${todayInfo.dayOfMonth})"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                // 30-Day Calendar Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            weekdays.forEach { wd ->
                                Text(
                                    text = wd,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val totalSlots = firstDayOffset + days.size
                        val weeks = (totalSlots + 6) / 7
                        for (w in 0 until weeks) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (d in 0 until 7) {
                                    val idx = w * 7 + d - firstDayOffset
                                    if (idx !in days.indices) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    } else {
                                        val info = days[idx]
                                        val completion = dailyCompletions[info.dayOfMonth]
                                        CalendarDayCell(
                                            info = info,
                                            completion = completion,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onPlayDay(info.epochDay, info.dayOfMonth) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    info: CalendarDayInfo,
    completion: DailyChallengeEntity?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isCompleted = completion != null
    val bgColor = when {
        info.isToday -> CobaltPrimary
        isCompleted -> EmeraldContainer
        !info.isFuture -> IceSurface
        else -> PureWhite
    }
    val textColor = when {
        info.isToday -> PureWhite
        isCompleted -> EmeraldSuccess
        !info.isFuture -> CobaltDark
        else -> MutedSlate
    }

    Surface(
        onClick = onClick,
        enabled = !info.isFuture,
        modifier = modifier
            .aspectRatio(0.92f)
            .testTag("daily_day_${info.dayOfMonth}"),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(
            width = if (info.isToday) 2.dp else 1.dp,
            color = if (info.isToday) CobaltDark else SoftBorderBlue
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${info.dayOfMonth}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            when {
                isCompleted -> Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Completed",
                    tint = if (info.isToday) PureWhite else EmeraldSuccess,
                    modifier = Modifier.size(14.dp)
                )
                info.isFuture -> Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Locked",
                    tint = MutedSlate,
                    modifier = Modifier.size(12.dp)
                )
                info.isToday -> Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = "Today",
                    tint = GoldAccent,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
