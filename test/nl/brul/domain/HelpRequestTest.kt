package nl.brul.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HelpRequestTest {

    private val creator = User(
        id = "user-1",
        username = "creator",
        role = Role.USER
    )

    private val helper = User(
        id = "user-2",
        username = "helper",
        role = Role.USER
    )

    private val bystander = User(
        id = "user-3",
        username = "bystander",
        role = Role.USER
    )

    private val location = Location(
        latitude = 52.0705,
        longitude = 4.3007
    )

    @Test
    fun `open help request can be claimed`() {
        val helpRequest = createHelpRequest()

        helpRequest.claim(helper)

        assertEquals(helper, helpRequest.claimedBy)
        assertEquals(ReportStatus.CLAIMED, helpRequest.status)
    }

    @Test
    fun `creator cannot claim own help request`() {
        val helpRequest = createHelpRequest()

        val exception =
            assertFailsWith<IllegalStateException> {
                helpRequest.claim(creator)
            }

        assertEquals("You cannot claim your own HelpRequest.", exception.message)
        assertEquals(ReportStatus.OPEN, helpRequest.status)
        assertNull(helpRequest.claimedBy)
    }

    @Test
    fun `claimed help request cannot be claimed again`() {
        val helpRequest = createHelpRequest()

        helpRequest.claim(helper)

        assertFailsWith<IllegalStateException> {
            helpRequest.claim(bystander)
        }

        assertEquals(helper, helpRequest.claimedBy)
    }

    @Test
    fun `resolved help request cannot be claimed`() {
        val helpRequest = createHelpRequest()

        helpRequest.resolve(resolvedBy = creator)

        assertFailsWith<IllegalStateException> {
            helpRequest.claim(helper)
        }
    }

    @Test
    fun `helper who claimed the request can resolve it`() {
        val helpRequest = createHelpRequest()

        helpRequest.claim(helper)
        helpRequest.resolve(resolvedBy = helper)

        assertEquals(ReportStatus.RESOLVED, helpRequest.status)
    }

    @Test
    fun `creator can resolve a claimed help request`() {
        val helpRequest = createHelpRequest()

        helpRequest.claim(helper)
        helpRequest.resolve(resolvedBy = creator)

        assertEquals(ReportStatus.RESOLVED, helpRequest.status)
    }

    @Test
    fun `user who did not claim the request cannot resolve it`() {
        val helpRequest = createHelpRequest()

        helpRequest.claim(helper)

        assertFailsWith<NotAllowedException> {
            helpRequest.resolve(resolvedBy = bystander)
        }

        assertEquals(ReportStatus.CLAIMED, helpRequest.status)
    }

    @Test
    fun `helper may resolve but not manage the request`() {
        val helpRequest = createHelpRequest()

        helpRequest.claim(helper)

        // Polymorphism: HelpRequest widens canBeResolvedBy,
        // canBeManagedBy stays the same as for every Report.
        assertTrue(helpRequest.canBeResolvedBy(helper))
        assertFalse(helpRequest.canBeManagedBy(helper))
    }

    private fun createHelpRequest(): HelpRequest {
        return HelpRequest(
            title = "Need help carrying groceries",
            description = "Could use some help this afternoon.",
            photo = null,
            location = location,
            createdBy = creator
        )
    }
}
