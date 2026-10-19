// src/nl/brul/api/features/report/model/Report.kt
package nl.brul.api.features.report.model

abstract class Report(
    val id: Long,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val location: Location,
    val status: ReportStatus = ReportStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis(),
    val viewCount: Int = 0
) {
    init {
        require(id > 0) {
            "Report ID must be positive."
        }
        require(title.isNotBlank()) {
            "Report title cannot be blank."
        }
        require(description.isNotBlank()) {
            "Report description cannot be blank."
        }
        require(createdAt > 0) {
            "Creation timestamp must be positive."
        }
        require(viewCount >= 0) {
            "View count cannot be negative."
        }
    }

    abstract val type: ReportType

    protected abstract fun validateCategory()
}