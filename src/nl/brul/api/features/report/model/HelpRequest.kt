// src/nl/brul/api/features/report/model/HelpRequest.kt
package nl.brul.api.features.report.model

import java.time.Duration

/**
 * Laag: model (het domein).
 *
 * Hulpvraag, erft van Report.
 * Toegestane categorieën: GROCERIES, TRANSPORT, MOVING, CHORES en OTHER.
 * Extra velden:
 * - `volunteersNeeded`, minimaal 1;
 * - `slot`, het tijdslot waarin geholpen wordt, maximaal 8 uur.
 * Klopt een van beide niet, dan volgt een IllegalArgumentException en bestaat het object nooit.
 *
 * Door het tijdslot werkt een hulpvraag als een reservering: vrijwilligers schrijven zich in
 * voor een tijd, mogen niet op twee overlappende hulpvragen tegelijk staan, en de hulpvraag
 * doorloopt fases naarmate de tijd verstrijkt (zie HelpRequestBooking in de feature help).
 *
 * De feature help gebruikt deze klasse om deelnemers te beheren.
 * ReportService zorgt dat `volunteersNeeded` nooit lager wordt dan het aantal deelnemers.
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
