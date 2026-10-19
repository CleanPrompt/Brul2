// src/nl/brul/api/features/report/model/HelpRequest.kt
package nl.brul.api.features.report.model

class HelpRequest(
    id: Long,
    title: String,
    description: String,
    category: ReportCategory,
    location: Location,
    val volunteersNeeded: Int,
    status: ReportStatus = ReportStatus.OPEN
) : Report(id, title, description, category, location, status) {

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