// test/nl/brul/api/features/vote/model/VoteModelTest.kt
package nl.brul.api.features.vote.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VoteModelTest {

    @Test
    fun `LIKE is a supported vote type`() {
        assertEquals(
            VoteType.LIKE,
            VoteType.valueOf("LIKE")
        )
    }

    @Test
    fun `DISLIKE is a supported vote type`() {
        assertEquals(
            VoteType.DISLIKE,
            VoteType.valueOf("DISLIKE")
        )
    }

    @Test
    fun `valid vote can be created`() {
        val vote = Vote(
            id = 1,
            reportId = 10,
            userId = 20,
            type = VoteType.LIKE
        )

        assertEquals(1L, vote.id)
        assertEquals(10L, vote.reportId)
        assertEquals(20L, vote.userId)
        assertEquals(VoteType.LIKE, vote.type)
    }

    @Test
    fun `vote rejects invalid ID`() {
        assertFailsWith<IllegalArgumentException> {
            Vote(
                id = 0,
                reportId = 10,
                userId = 20,
                type = VoteType.LIKE
            )
        }
    }

    @Test
    fun `vote rejects invalid report ID`() {
        assertFailsWith<IllegalArgumentException> {
            Vote(
                id = 1,
                reportId = -1,
                userId = 20,
                type = VoteType.LIKE
            )
        }
    }

    @Test
    fun `vote rejects invalid user ID`() {
        assertFailsWith<IllegalArgumentException> {
            Vote(
                id = 1,
                reportId = 10,
                userId = 0,
                type = VoteType.DISLIKE
            )
        }
    }

    @Test
    fun `vote summary calculates score`() {
        val summary = VoteSummary(
            reportId = 10,
            likes = 8,
            dislikes = 3
        )

        assertEquals(5, summary.score)
    }

    @Test
    fun `vote summary calculates total votes`() {
        val summary = VoteSummary(
            reportId = 10,
            likes = 8,
            dislikes = 3
        )

        assertEquals(11, summary.totalVotes)
    }
}