package nl.brul.domain

class HelpRequest(
    title: String,
    description: String?,
    photo: String?,
    location: Location,
    createdBy: User
) : Report(
    title = title,
    description = description,
    photo = photo,
    location = location,
    createdBy = createdBy
), Claimable {

    var claimedBy: User? = null
        private set

    override fun claim(user: User) {
        check(status == ReportStatus.OPEN) {
            "Only an open HelpRequest can be claimed."
        }

        claimedBy = user
        status = ReportStatus.CLAIMED
    }
}