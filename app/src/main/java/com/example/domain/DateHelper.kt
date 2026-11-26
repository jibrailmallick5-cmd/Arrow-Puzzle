package com.example.domain

import java.util.Calendar
import java.util.Locale

data class CalendarDayInfo(
    val dayOfMonth: Int,
    val epochDay: Long,
    val isToday: Boolean,
    val isFuture: Boolean
)

object DateHelper {

    fun currentEpochDay(): Long {
        val cal = Calendar.getInstance()
        return epochDayFor(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun currentYearMonthKey(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    fun currentMonthTitle(): String {
        val cal = Calendar.getInstance()
        val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) ?: "Month"
        val year = cal.get(Calendar.YEAR)
        return "$monthName $year"
    }

    fun currentMonthDays(): List<CalendarDayInfo> {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val todayDom = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val count = daysInMonth.coerceAtLeast(30).coerceAtMost(31)

        return (1..count).map { dom ->
            val epoch = epochDayFor(year, month, dom)
            CalendarDayInfo(
                dayOfMonth = dom,
                epochDay = epoch,
                isToday = dom == todayDom,
                isFuture = dom > todayDom
            )
        }
    }

    fun firstDayOfMonthOffset(): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        // Sunday = 1 -> offset 0 .. Saturday = 7 -> offset 6
        return (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY).coerceIn(0, 6)
    }

    fun currentTournamentEventId(): Int {
        // Weekly tournament cycle ID
        return (currentEpochDay() / 7L).toInt().coerceAtLeast(1)
    }

    fun remainingSecondsInCurrentTournament(): Long {
        val cal = Calendar.getInstance()
        val nowMillis = cal.timeInMillis
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysUntilEnd = (Calendar.SATURDAY - dayOfWeek + 7) % 7 + 1
        cal.add(Calendar.DAY_OF_YEAR, daysUntilEnd)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        return ((cal.timeInMillis - nowMillis) / 1000L).coerceAtLeast(3600L)
    }

    fun epochDayFor(year: Int, month1Based: Int, dayOfMonth: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month1Based - 1)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis / 86_400_000L
    }
}
