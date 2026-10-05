package nl.brul.domain

import java.time.LocalDateTime
import java.util.UUID

sealed class Report(
    title: String,
    description: String?,
    photo: String?,
    location: Location,
    val createdBy: User
) {
    val id: String = UUID.randomUUID().toString()

    val createdAt: LocalDateTime = LocalDateTime.now()

    var title: String = title
        private set

    var description: String? = description
        private set

    var photo: String? = photo
        private set

    var location: Location = location
        private set

    var status: ReportStatus = ReportStatus.OPEN
        protected set

    init {
        require(title.isNotBlank()) {
            "Title must not be blank."
        }
    }

    fun update(
        title: String,
        description: String?,
        photo: String?,
        location: Location
    ) {
        require(title.isNotBlank()) {
            "Title must not be blank."
        }

        this.title = title
        this.description = description
        this.photo = photo
        this.location = location
    }

    fun resolve() {
        check(status != ReportStatus.RESOLVED) {
            "Report is already resolved."
        }

        status = ReportStatus.RESOLVED
    }
}