// src/nl/brul/api/features/report/model/SafetyReport.kt
package nl.brul.api.features.report.model

class SafetyReport(
    id: Long,
    title: String,
    description: String,
    category: ReportCategory,
    location: Location,
    val urgent: Boolean,
    status: ReportStatus = ReportStatus.OPEN
) : Report(id, title, description, category, location, status) {

    override val type: ReportType = ReportType.SAFETY

    init {
        validateCategory()
    }

    override fun validateCategory() {
        require(
            category in setOf(
                ReportCategory.THEFT,
                ReportCategory.SUSPICIOUS_SITUATION,
                ReportCategory.TRAFFIC_SAFETY
            )
        ) {
            "Invalid safety report category."
        }
    }
}