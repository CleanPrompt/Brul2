package nl.brul.domain

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A report made by a resident about something in the neighbourhood.
 *
 * The class is sealed: the set of report types is closed ({complete, disjoint})
 * and every subtype is known at compile time, so `when` expressions over a
 * [Report] are exhaustive.
 *
 * A report guards its own state: properties can only change through
 * [update], [resolve] and (for a [HelpRequest]) `claim`, and each of those
 * methods checks both who is asking and whether the current status allows it.
 */
sealed class Report(
    title: String,
    description: String?,
    photo: String?,
    location: Location,
    val createdBy: User,
    val createdAt: Instant = Clock.System.now(),
    val id: String = newReportId()
) {

    /**
     * Every subtype states which kind of report it is.
     */
    abstract val type: ReportType

    var title: String = requireValidTitle(title)
        private set

    var description: String? = description
        private set

    var photo: String? = photo
        private set

    var location: Location = location
        private set

    var status: ReportStatus = ReportStatus.OPEN
        protected set

    val isResolved: Boolean
        get() = status == ReportStatus.RESOLVED

    fun isCreatedBy(user: User): Boolean =
        createdBy.id == user.id

    /**
     * Only the creator of a report or an admin may change or delete it.
     */
    fun canBeManagedBy(user: User): Boolean =
        isCreatedBy(user) || user.isAdmin

    /**
     * Who may resolve this report. By default the same users who may manage it;
     * subtypes can widen this rule (see [HelpRequest]).
     */
    open fun canBeResolvedBy(user: User): Boolean =
        canBeManagedBy(user)

    /**
     * Throws [NotAllowedException] when [user] may not manage this report.
     */
    fun checkCanBeManagedBy(user: User) {
        if (!canBeManagedBy(user)) {
            throw NotAllowedException(
                "You are not allowed to manage this report."
            )
        }
    }

    fun update(
        title: String,
        description: String?,
        photo: String?,
        location: Location,
        updatedBy: User
    ) {
        checkCanBeManagedBy(updatedBy)

        check(!isResolved) {
            "A resolved report can no longer be changed."
        }

        this.title = requireValidTitle(title)
        this.description = description
        this.photo = photo
        this.location = location
    }

    fun resolve(resolvedBy: User) {
        if (!canBeResolvedBy(resolvedBy)) {
            throw NotAllowedException(
                "You are not allowed to resolve this report."
            )
        }

        check(!isResolved) {
            "Report is already resolved."
        }

        status = ReportStatus.RESOLVED
    }

    /**
     * Case-insensitive search in the title and the description.
     */
    fun matchesSearch(searchTerm: String): Boolean {
        val term = searchTerm.trim()

        return title.contains(term, ignoreCase = true) ||
                description?.contains(term, ignoreCase = true) == true
    }

    private companion object {

        fun requireValidTitle(title: String): String {
            require(title.isNotBlank()) {
                "Title must not be blank."
            }

            return title
        }

        @OptIn(ExperimentalUuidApi::class)
        fun newReportId(): String =
            Uuid.random().toString()
    }
}
