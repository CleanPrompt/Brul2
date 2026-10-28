// test/nl/brul/api/features/help/service/HelpParticipationServiceTest.kt
package nl.brul.api.features.help.service

import java.time.Duration
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import nl.brul.api.common.error.DomainConflictException
import nl.brul.api.database.ExposedTransactionRunner
import nl.brul.api.features.help.model.HelpRequestPhase
import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.help.repository.ExposedHelpParticipationRepository
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.TimeSlot
import nl.brul.api.features.report.repository.ExposedReportRepository
import nl.brul.api.features.report.service.ReportService
import nl.brul.api.support.TestClock
import nl.brul.api.support.TestDatabase
import nl.brul.api.support.createHelpRequest

/**
 * Service tests against a real (in-memory) H2 database with a fixed clock.
 * "now" starts at 08:00; help request H runs from 10:00 to 12:00.
 */
class HelpParticipationServiceTest {

    private fun at(time: String): Instant = Instant.parse("2026-11-20T${time}:00Z")

    private class Fixture(places: Int) {
        val database = TestDatabase.create("help_service", users = 6)
        val clock = TestClock(Instant.parse("2026-11-20T08:00:00Z"))
        val reports = ExposedReportRepository(database)
        val participations = ExposedHelpParticipationRepository(database)

        val service = HelpParticipationService(
            participationRepository = participations,
            reportRepository = reports,
            clock = clock,
            transactions = ExposedTransactionRunner(database)
        )

        val reportService = ReportService(
            repository = reports,
            helpParticipationRepository = participations,
            clock = clock,
            participationService = service
        )

        val helpRequest: HelpRequest = reports.createHelpRequest(
            slot = TimeSlot(
                Instant.parse("2026-11-20T10:00:00Z"),
                Instant.parse("2026-11-20T12:00:00Z")
            ),
            volunteersNeeded = places
        )

        fun statusOf(userId: Long): ParticipationStatus? =
            participations.findByHelpRequestAndUser(helpRequest.id, userId)?.status

        fun copyOfRequest(
            places: Int = helpRequest.volunteersNeeded,
            status: ReportStatus = helpRequest.status,
            slot: TimeSlot = helpRequest.slot
        ) = HelpRequest(
            id = helpRequest.id,
            title = helpRequest.title,
            description = helpRequest.description,
            category = helpRequest.category,
            location = helpRequest.location,
            volunteersNeeded = places,
            slot = slot,
            status = status,
            createdAt = helpRequest.createdAt,
            ownerId = helpRequest.ownerId
        )
    }

    @Test
    fun `user can join an open help request`() {
        val f = Fixture(places = 2)

        val result = f.service.join(f.helpRequest.id, 2)

        assertEquals(ParticipationStatus.SIGNED_UP, result.participation.status)
        assertNull(result.waitlistPosition)

        val summary = f.service.getSummary(f.helpRequest.id)

        assertEquals(1, summary.participantCount)
        assertEquals(1, summary.availablePlaces)
        assertEquals(HelpRequestPhase.OPEN, summary.phase)
    }

    @Test
    fun `owner cannot join their own help request`() {
        val f = Fixture(places = 2)

        assertFailsWith<HelpParticipationForbiddenException> {
            f.service.join(f.helpRequest.id, 1)
        }
    }

    @Test
    fun `full help request leads to the waitlist`() {
        val f = Fixture(places = 1)

        f.service.join(f.helpRequest.id, 2)
        val second = f.service.join(f.helpRequest.id, 3)

        assertEquals(ParticipationStatus.WAITLISTED, second.participation.status)
        assertEquals(1, second.waitlistPosition)
        assertEquals(HelpRequestPhase.FULL, f.service.getSummary(f.helpRequest.id).phase)
    }

    // T11 at service level (R6 + R8)
    @Test
    fun `leaving promotes the first volunteer on the waitlist`() {
        val f = Fixture(places = 1)

        f.service.join(f.helpRequest.id, 2)
        f.service.join(f.helpRequest.id, 3)

        val left = f.service.leave(f.helpRequest.id, 2)

        assertEquals(ParticipationStatus.CANCELLED, left.status)
        assertEquals(ParticipationStatus.SIGNED_UP, f.statusOf(3))

        val summary = f.service.getSummary(f.helpRequest.id)

        assertEquals(1, summary.participantCount)
        assertEquals(0, summary.waitlistCount)
    }

    @Test
    fun `leaving late is recorded as a late cancellation`() {
        val f = Fixture(places = 1)

        f.service.join(f.helpRequest.id, 2)
        f.clock.set("2026-11-20T08:30:00Z")

        assertEquals(
            ParticipationStatus.LATE_CANCELLED,
            f.service.leave(f.helpRequest.id, 2).status
        )
    }

    @Test
    fun `cannot leave without participating`() {
        val f = Fixture(places = 1)

        assertFailsWith<HelpParticipationNotFoundException> {
            f.service.leave(f.helpRequest.id, 2)
        }
    }

    @Test
    fun `waitlisted volunteer who is busy elsewhere is skipped (R8)`() {
        val f = Fixture(places = 1)

        // User 3 joins the waitlist of H, then signs up for G (11:00-13:00).
        f.service.join(f.helpRequest.id, 2)
        f.service.join(f.helpRequest.id, 3)
        f.service.join(f.helpRequest.id, 4)

        val other = f.reports.createHelpRequest(
            slot = TimeSlot(at("11:00"), at("13:00")),
            volunteersNeeded = 1,
            ownerId = 5,
            title = "Overlapping request"
        )

        f.service.join(other.id, 3)

        f.service.leave(f.helpRequest.id, 2)

        assertEquals(ParticipationStatus.WAITLISTED, f.statusOf(3))
        assertEquals(ParticipationStatus.SIGNED_UP, f.statusOf(4))
    }

    @Test
    fun `volunteer with three recent no-shows is blocked (R11)`() {
        val f = Fixture(places = 2)

        repeat(3) { index ->
            val past = f.reports.createHelpRequest(
                slot = TimeSlot(
                    at("10:00").minus(Duration.ofDays(index + 2L)),
                    at("12:00").minus(Duration.ofDays(index + 2L))
                ),
                ownerId = 5,
                title = "Past request $index"
            )

            val added = f.participations.add(past.id, 2, joinedAt = at("06:00").minus(Duration.ofDays(10)))

            f.participations.update(
                added.moveTo(ParticipationStatus.NO_SHOW, at("12:30").minus(Duration.ofDays(index + 2L)))
            )
        }

        val error = assertFailsWith<HelpParticipationConflictException> {
            f.service.join(f.helpRequest.id, 2)
        }

        assertEquals("BLOCKED", error.code)
        assertEquals(true, f.service.getReliability(2).blocked)
    }

    @Test
    fun `owner records attendance after the start (R9)`() {
        val f = Fixture(places = 1)

        f.service.join(f.helpRequest.id, 2)

        assertFailsWith<HelpParticipationConflictException> {
            f.service.markAttendance(f.helpRequest.id, 1, 2, attended = true)
        }

        f.clock.set("2026-11-20T12:30:00Z")

        val marked = f.service.markAttendance(f.helpRequest.id, 1, 2, attended = false)

        assertEquals(ParticipationStatus.NO_SHOW, marked.status)
        assertEquals(1, f.service.getReliability(2).noShowsInWindow)
    }

    @Test
    fun `extra places promote the waitlist (R10)`() {
        val f = Fixture(places = 1)

        f.service.join(f.helpRequest.id, 2)
        f.service.join(f.helpRequest.id, 3)

        f.reportService.update(f.copyOfRequest(places = 2))

        assertEquals(ParticipationStatus.SIGNED_UP, f.statusOf(3))
    }

    @Test
    fun `closing before the start cancels everyone (R13)`() {
        val f = Fixture(places = 1)

        f.service.join(f.helpRequest.id, 2)
        f.service.join(f.helpRequest.id, 3)

        f.reportService.update(f.copyOfRequest(status = ReportStatus.RESOLVED))

        assertEquals(ParticipationStatus.CANCELLED, f.statusOf(2))
        assertEquals(ParticipationStatus.CANCELLED, f.statusOf(3))
        assertEquals(HelpRequestPhase.CLOSED, f.service.getSummary(f.helpRequest.id).phase)
    }

    @Test
    fun `slot cannot move while volunteers are signed up (R12)`() {
        val f = Fixture(places = 1)

        f.service.join(f.helpRequest.id, 2)

        val error = assertFailsWith<DomainConflictException> {
            f.reportService.update(
                f.copyOfRequest(slot = TimeSlot(at("14:00"), at("16:00")))
            )
        }

        assertEquals("SLOT_LOCKED", error.code)
    }

    @Test
    fun `agenda shows upcoming participations in time order`() {
        val f = Fixture(places = 2)

        val earlier = f.reports.createHelpRequest(
            slot = TimeSlot(at("08:30"), at("09:30")),
            ownerId = 5,
            title = "Earlier request"
        )

        f.service.join(f.helpRequest.id, 2)
        f.service.join(earlier.id, 2)

        assertEquals(
            listOf(earlier.id, f.helpRequest.id),
            f.service.getAgenda(2, upcomingOnly = true).map { it.helpRequest.id }
        )

        f.clock.set("2026-11-20T09:45:00Z")

        assertEquals(
            listOf(f.helpRequest.id),
            f.service.getAgenda(2, upcomingOnly = true).map { it.helpRequest.id }
        )
    }

    // T14: two volunteers try to take the last place at the same moment.
    @Test
    fun `concurrent sign-ups never overbook the last place`() {
        repeat(5) {
            val f = Fixture(places = 1)
            val ready = CountDownLatch(2)
            val go = CountDownLatch(1)
            val pool = Executors.newFixedThreadPool(2)

            val jobs = listOf(2L, 3L).map { userId ->
                pool.submit<ParticipationStatus> {
                    ready.countDown()
                    go.await()
                    f.service.join(f.helpRequest.id, userId).participation.status
                }
            }

            ready.await()
            go.countDown()

            val statuses = jobs.map { it.get(10, TimeUnit.SECONDS) }
            pool.shutdown()

            assertEquals(1, statuses.count { it == ParticipationStatus.SIGNED_UP })
            assertEquals(1, statuses.count { it == ParticipationStatus.WAITLISTED })
            assertEquals(1, f.participations.countSignedUp(f.helpRequest.id))
        }
    }

    @Test
    fun `unknown help request is rejected`() {
        val f = Fixture(places = 1)

        assertFailsWith<HelpRequestNotFoundException> {
            f.service.join(999999, 2)
        }

        assertFailsWith<HelpRequestNotFoundException> {
            f.service.getSummary(999999)
        }
    }
}
