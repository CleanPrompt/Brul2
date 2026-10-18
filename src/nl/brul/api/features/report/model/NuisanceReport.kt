// src/nl/brul/api/features/report/model/NuisanceReport.kt
package nl.brul.api.features.report.model

class NuisanceReport(
    id: Long,
    title: String,
    description: String,
    location: Location,
    val category: String,
    status: ReportStatus = ReportStatus.OPEN
) : Report(id, title, description, location, status) {

    init {
        require(category.isNotBlank()) {
            "Nuisance category cannot be blank."
        }
    }

    override val type: ReportType = ReportType.NUISANCE

    override fun summary(): String {
        return "Nuisance: $title ($category)"
    }
}