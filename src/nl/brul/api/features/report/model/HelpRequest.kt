// src/nl/brul/api/features/report/model/HelpRequest.kt
package nl.brul.api.features.report.model

<<<<<<< Updated upstream
/**
 * Laag: model (het domein).
 *
 * Hulpvraag, erft van Report.
 * Toegestane categorieën: GROCERIES, TRANSPORT, MOVING, CHORES en OTHER.
 * Extra veld: `volunteersNeeded`, minimaal 1; anders volgt een IllegalArgumentException en bestaat
 * het object nooit.
 *
 * De feature help gebruikt deze klasse om deelnemers te beheren.
 * ReportService zorgt dat `volunteersNeeded` nooit lager wordt dan het aantal deelnemers.
=======
import java.time.Duration

/**
 * A request for help from neighbours, planned in a fixed [slot].
 *
 * The slot turns a help request into something like a reservation:
 * volunteers sign up for a time, can overlap with other help requests,
 * and the request moves through phases as time passes
 * (see HelpRequestBooking in the help feature).
>>>>>>> Stashed changes
 */
class HelpRequest(
    id: Long,
    title: String,
    description: String,
    category: ReportCategory,
    location: Location,
    val volunteersNeeded: Int,
    val slot: TimeSlot,
    status: ReportStatus = ReportStatus.OPEN,
    createdAt: Long = System.currentTimeMillis(),
    viewCount: Int = 0,
    ownerId: Long? = null
) : Report(
    id, title, description, category, location,
    status, createdAt, viewCount, ownerId
) {
    override val type: ReportType = ReportType.HELP_REQUEST

    init {
        validateCategory()

        require(volunteersNeeded > 0) {
            "At least one volunteer must be needed."
        }

        require(slot.duration <= MAX_SLOT_DURATION) {
            "A help request can last at most ${MAX_SLOT_DURATION.toHours()} hours."
        }
    }

    override fun validateCategory() {
        require(category in ReportType.HELP_REQUEST.allowedCategories) {
            "Invalid help request category."
        }
    }

    companion object {
        /** R1: maximum length of a help request (agreed value, easy to change). */
        val MAX_SLOT_DURATION: Duration = Duration.ofHours(8)
    }
}
