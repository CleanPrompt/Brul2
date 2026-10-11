package nl.brul.domain

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A request for help that another resident can claim.
 */
class HelpRequest(
    title: String,
    description: String?,
    photo: String?,
    location: Location,
    createdBy: User,
    createdAt: Instant = Clock.System.now()
) : Report(
    title = title,
    description = description,
    photo = photo,
    location = location,
    createdBy = createdBy,
    createdAt = createdAt
), Claimable {

    override val type: ReportType = ReportType.HELP

    override var claimedBy: User? = null
        private set

    fun isClaimedBy(user: User): Boolean =
        claimedBy?.id == user.id

    override fun claim(user: User) {
        check(!isCreatedBy(user)) {
            "You cannot claim your own HelpRequest."
        }

        check(status == ReportStatus.OPEN) {
            "Only an open HelpRequest can be claimed."
        }

        claimedBy = user
        status = ReportStatus.CLAIMED
    }

    /**
     * Besides the creator and an admin, the helper who claimed the request
     * may also mark it as resolved.
     */
    override fun canBeResolvedBy(user: User): Boolean =
        super.canBeResolvedBy(user) || isClaimedBy(user)
}
