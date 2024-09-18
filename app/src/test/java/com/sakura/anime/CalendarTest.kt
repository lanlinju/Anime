package com.sakura.anime

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class CalendarTest {

    @Test
    fun testDay() {
        val calendar = Calendar.getInstance()

        val daysOfWeek = listOf(
            Calendar.MONDAY to 0,
            Calendar.TUESDAY to 1,
            Calendar.WEDNESDAY to 2,
            Calendar.THURSDAY to 3,
            Calendar.FRIDAY to 4,
            Calendar.SATURDAY to 5,
            Calendar.SUNDAY to 6,
        )

        for ((calendarDay, expectedDay) in daysOfWeek) {
            calendar.set(Calendar.DAY_OF_WEEK, calendarDay)

            var day = calendar.get(Calendar.DAY_OF_WEEK) - 2
            if (day == -1) {
                day = 6
            }

            assertEquals(expectedDay, day)
        }
    }
}
