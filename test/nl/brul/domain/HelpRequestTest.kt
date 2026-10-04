package nl.brul.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun `claimed help request cannot be claimed again`() {
        val helpRequest = createHelpRequest()

        helpRequest.claim(helper)

        assertFailsWith<IllegalStateException> {
            helpRequest.claim(
                User(
                    id = "user-3",
                    username = "anotherHelper",
                    role = Role.USER
                )
            )
        }
    }

    @Test
    fun `resolved help request cannot be claimed`() {
        val helpRequest = createHelpRequest()

        helpRequest.resolve()

        assertFailsWith<IllegalStateException> {
            helpRequest.claim(helper)
        }
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