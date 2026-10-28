// test/nl/brul/api/features/help/model/ParticipationStatusTest.kt
package nl.brul.api.features.help.model

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import nl.brul.api.features.help.model.ParticipationStatus.ATTENDED
import nl.brul.api.features.help.model.ParticipationStatus.CANCELLED
import nl.brul.api.features.help.model.ParticipationStatus.LATE_CANCELLED
import nl.brul.api.features.help.model.ParticipationStatus.NO_SHOW
import nl.brul.api.features.help.model.ParticipationStatus.SIGNED_UP
import nl.brul.api.features.help.model.ParticipationStatus.WAITLISTED

class ParticipationStatusTest {

    @Test
    fun `allowed transitions follow the state machine`() {
        val allowed = mapOf(
            WAITLISTED to setOf(SIGNED_UP, CANCELLED),
            SIGNED_UP to setOf(CANCELLED, LATE_CANCELLED, ATTENDED, NO_SHOW),
            CANCELLED to setOf(SIGNED_UP, WAITLISTED),
            LATE_CANCELLED to emptySet(),
            ATTENDED to emptySet(),
            NO_SHOW to emptySet()
        )

        for (from in ParticipationStatus.entries) {
            for (to in ParticipationStatus.entries) {
                assertEquals(
                    to in allowed.getValue(from),
                    from.canMoveTo(to),
                    "$from -> $to"
                )
            }
        }
    }

    @Test
    fun `only waitlisted and signed up are active`() {
        assertTrue(WAITLISTED.isActive)
        assertTrue(SIGNED_UP.isActive)
        assertFalse(CANCELLED.isActive)
        assertFalse(NO_SHOW.isActive)
    }

    @Test
    fun `moving to a forbidden status fails`() {
        val participation = HelpParticipation(
            id = 1,
            helpRequestId = 1,
            userId = 2,
            status = ATTENDED
        )

        assertFailsWith<IllegalArgumentException> {
            participation.moveTo(SIGNED_UP, Instant.parse("2026-11-20T08:00:00Z"))
        }
    }

    @Test
    fun `signing up again after cancelling resets the queue position`() {
        val joined = Instant.parse("2026-11-19T08:00:00Z")
        val rejoined = Instant.parse("2026-11-20T08:00:00Z")

        val cancelled = HelpParticipation(
            id = 1,
            helpRequestId = 1,
            userId = 2,
            status = CANCELLED,
            joinedAt = joined
        )

        val again = cancelled.moveTo(SIGNED_UP, rejoined)

        assertEquals(SIGNED_UP, again.status)
        assertEquals(rejoined, again.joinedAt)
        assertEquals(rejoined, again.changedAt)
    }
}
