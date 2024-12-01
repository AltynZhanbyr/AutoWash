package com.example.autowash.util

import com.example.autowash.core.model.DaysCalendar
import java.time.DayOfWeek
import java.time.LocalDate

fun getCalendar(interval: Int = 10): List<DaysCalendar> {
    val startDate = LocalDate.now()

    return (0 until interval).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        val dayOfWeek = dayOfWeekMapper[date.dayOfWeek] ?: "пн"
        val day = date.dayOfMonth

        DaysCalendar(dayOfWeek, day)
    }
}

private val dayOfWeekMapper = mapOf(
    DayOfWeek.MONDAY to "пн",
    DayOfWeek.TUESDAY to "вт",
    DayOfWeek.WEDNESDAY to "ср",
    DayOfWeek.THURSDAY to "чт",
    DayOfWeek.FRIDAY to "пт",
    DayOfWeek.SATURDAY to "сб",
    DayOfWeek.SUNDAY to "вс"
)