package nl.brul.domain

import java.time.LocalDateTime
import java.util.UUID

abstract class Report(
    val title: String,
    val description: String?,
    val photo: String?,
    val location: Location,
    val createdBy: User
) {
    val id: String = UUID.randomUUID().toString()

    val createdAt: LocalDateTime = LocalDateTime.now()

    var status: ReportStatus = ReportStatus.OPEN
        protected set

    init {
        require(title.isNotBlank()) {
            "Title must not be blank."
        }
    }

    fun resolve() {
        check(status != ReportStatus.RESOLVED) {
            "Report is already resolved."
        }

        status = ReportStatus.RESOLVED
    }
}