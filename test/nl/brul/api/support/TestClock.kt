// test/nl/brul/api/support/TestClock.kt
package nl.brul.api.support

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * A clock that tests can set and move, so time-based rules can be
 * checked at exact moments ("08:00", "two hours before the start").
 */
class TestClock(
    @Volatile var now: Instant
) : Clock() {

    override fun instant(): Instant = now

    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId?): Clock = this

    fun set(moment: String) {
        now = Instant.parse(moment)
    }

    fun advance(duration: Duration) {
        now = now.plus(duration)
    }
}
