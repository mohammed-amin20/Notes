package com.mohammed.notes.feature.core.security

import org.junit.Assert.assertEquals
import org.junit.Test

class PinThrottleTest {

    @Test
    fun `failures below the arm threshold cost nothing`() {
        assertEquals(0L, PinThrottle.cooldownFor(0))
        assertEquals(0L, PinThrottle.cooldownFor(1))
        assertEquals(0L, PinThrottle.cooldownFor(PinThrottle.ARM_AT_FAILURES - 1))
    }

    @Test
    fun `the schedule escalates exactly as specified`() {
        assertEquals(30_000L, PinThrottle.cooldownFor(5))
        assertEquals(60_000L, PinThrottle.cooldownFor(6))
        assertEquals(5 * 60_000L, PinThrottle.cooldownFor(7))
        assertEquals(15 * 60_000L, PinThrottle.cooldownFor(8))
        assertEquals(30 * 60_000L, PinThrottle.cooldownFor(9))
        assertEquals(60 * 60_000L, PinThrottle.cooldownFor(10))
    }

    @Test
    fun `the schedule never shrinks as failures grow`() {
        var previous = 0L
        for (failures in 0..30) {
            val cooldown = PinThrottle.cooldownFor(failures)
            check(cooldown >= previous) {
                "cooldown shrank at $failures failures: $cooldown < $previous"
            }
            previous = cooldown
        }
    }

    @Test
    fun `the schedule caps at one hour`() {
        assertEquals(60 * 60_000L, PinThrottle.cooldownFor(11))
        assertEquals(60 * 60_000L, PinThrottle.cooldownFor(1_000))
    }

    @Test
    fun `negative failure counts are treated like zero`() {
        assertEquals(0L, PinThrottle.cooldownFor(-1))
    }
}
