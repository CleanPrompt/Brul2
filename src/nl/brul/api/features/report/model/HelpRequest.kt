// src/nl/brul/api/features/report/model/HelpRequest.kt
package nl.brul.api.features.report.model

class HelpRequest(
    id: Long,
    title: String,
    description: String,
    location: Location,
    val volunteersNeeded: Int,
    status: ReportStatus = ReportStatus.OPEN
) : Report(id, title, description, location, status) {

    init {
        require(volunteersNeeded > 0) {
            "At least one volunteer must be needed."
        }
    }

    override val type: ReportType = ReportType.HELP_REQUEST

    override fun summary(): String {
        return "Help: $title ($volunteersNeeded volunteers needed)"
    }
}