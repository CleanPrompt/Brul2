// src/nl/brul/api/features/report/model/NuisanceReport.kt
package nl.brul.api.features.report.model

class NuisanceReport(
    id: Long,
    title: String,
    description: String,
    category: ReportCategory,
    location: Location,
    status: ReportStatus = ReportStatus.OPEN
) : Report(id, title, description, category, location, status) {

    override val type: ReportType = ReportType.NUISANCE

    init {
        validateCategory()
    }

    override fun validateCategory() {
        require(
            category in setOf(
                ReportCategory.NOISE,
                ReportCategory.WASTE,
                ReportCategory.VANDALISM,
                ReportCategory.PARKING
            )
        ) {
            "Invalid nuisance report category."
        }
    }
}