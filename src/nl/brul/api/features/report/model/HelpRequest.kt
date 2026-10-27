// src/nl/brul/api/features/report/model/HelpRequest.kt
package nl.brul.api.features.report.model

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
 */
class HelpRequest(
    id: Long,
    title: String,
    description: String,
    category: ReportCategory,
    location: Location,
    val volunteersNeeded: Int,
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
    }

    override fun validateCategory() {
        require(
            category in setOf(
                ReportCategory.GROCERIES,
                ReportCategory.TRANSPORT,
                ReportCategory.MOVING,
                ReportCategory.CHORES,
                ReportCategory.OTHER
            )
        ) {
            "Invalid help request category."
        }
    }
}