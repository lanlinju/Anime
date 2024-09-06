package com.anime.danmaku

import com.anime.danmaku.api.Danmaku
import com.anime.danmaku.api.DanmakuEvent
import com.anime.danmaku.api.DanmakuLocation
import com.anime.danmaku.api.TimeBasedDanmakuSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class TimeBasedDanmakuSessionTest {

    @Test
    fun `test repopulate after seek`() = runBlocking {
        // Create a test list of Danmaku objects with different playTimeMillis
        val danmakuList = listOf(
            dummyDanmaku(1.0),
            dummyDanmaku(2.0),
            dummyDanmaku(3.0),
            dummyDanmaku(4.0)
        )

        // Create a TimeBasedDanmakuSession with the test list
        val session = TimeBasedDanmakuSession.create(danmakuList.asSequence())

        // Simulate the current time being 5000 milliseconds, requiring a repopulate
        val curTimeMillis = { 5000.milliseconds }

        // Collect the flow of danmaku events
        val events = session.at(curTimeMillis).take(1).first()

        // Assert that the repopulate event contains the correct danmaku
        assertTrue(events is DanmakuEvent.Repopulate)
        val repopulatedDanmaku = (events as DanmakuEvent.Repopulate).list
        assertEquals(4, repopulatedDanmaku.size) // Ensure 4 danmaku were repopulated
    }

    @Test
    fun `test danmaku flow emission`() = runBlocking {
        // Create a dummy list of danmakus
        val danmakus = listOf(
            dummyDanmaku(0.0, "Danmaku 0"),
            dummyDanmaku(1.0, "Danmaku 1"),
            dummyDanmaku(2.0, "Danmaku 2"),
            dummyDanmaku(3.0, "Danmaku 3"),
            dummyDanmaku(4.0, "Danmaku 4"),
            dummyDanmaku(5.0, "Danmaku 5"),
            dummyDanmaku(5.5, "Danmaku 5.5"),
            dummyDanmaku(7.0, "Danmaku 7"),
        )

        // Initialize a TimeBasedDanmakuSession
        val session = TimeBasedDanmakuSession.create(danmakus.asSequence())

        // Create a variable to represent the current time
        var curTimeMillis = 0L

        // A coroutine to simulate time progression
        val timeJob = launch {
            while (isActive) {
                delay(500) // Simulate half a second passing
                curTimeMillis += 500
            }
        }

        // Collect the emitted danmaku events
        val events = mutableListOf<DanmakuEvent>()
        val job = launch {
            session.at(curTimeMillis = { curTimeMillis.milliseconds })
                .collect {
                    println("currTime: ${curTimeMillis.milliseconds}")
                    events.add(it)
                }
        }

        // Wait for enough time to pass
        delay(8000)

        // Cancel the jobs
        timeJob.cancel()
        job.cancel()

        // Check that the correct danmakus were emitted
        assertEquals(1, (events[0] as DanmakuEvent.Repopulate).list.size)
        assertEquals("Danmaku 0", (events[0] as DanmakuEvent.Repopulate).list[0].text)
        assertEquals("Danmaku 1", (events[1] as DanmakuEvent.Add).danmaku.text)
        assertEquals("Danmaku 2", (events[2] as DanmakuEvent.Add).danmaku.text)
        assertEquals("Danmaku 3", (events[3] as DanmakuEvent.Add).danmaku.text)
        assertEquals("Danmaku 5.5", (events[6] as DanmakuEvent.Add).danmaku.text)
        assertEquals(8, events.size)
    }

    private fun dummyDanmaku(timeSecs: Double, text: String = "$timeSecs") =
        dummyDanmaku((timeSecs * 1000).toLong(), text)

    private fun dummyDanmaku(timeMillis: Long, text: String = "$timeMillis") =
        Danmaku(text, "dummy", timeMillis, text, DanmakuLocation.NORMAL, text, 0)
}