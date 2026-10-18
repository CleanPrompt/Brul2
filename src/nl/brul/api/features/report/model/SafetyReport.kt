// src/nl/brul/api/features/report/model/SafetyReport.kt
package nl.brul.api.features.report.model

class SafetyReport(
    id: Long,
    title: String,
    description: String,
    location: Location,
    val urgent: Boolean,
    status: ReportStatus = ReportStatus.OPEN
) : Report(id, title, description, location, status) {

    override val type: ReportType = ReportType.SAFETY

    override fun summary(): String {
        val priority = if (urgent) "Urgent" else "Normal"
        return "Safety: $title ($priority)"
    }
}