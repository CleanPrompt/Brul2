// src/nl/brul/api/features/report/model/SafetyReport.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * Veiligheidsmelding, erft van Report.
 * Toegestane categorieën: THEFT, SUSPICIOUS_SITUATION en TRAFFIC_SAFETY.
 * Extra veld: `urgent` (is het dringend?).
 * De `init` van Report draait eerst; daarna controleert deze klasse de categorie.
 */
class SafetyReport(
    id: Long,
    title: String,
    description: String,
    category: ReportCategory,
    location: Location,
    val urgent: Boolean,
    status: ReportStatus = ReportStatus.OPEN,
    createdAt: Long = System.currentTimeMillis(),
    viewCount: Int = 0,
    ownerId: Long? = null
) : Report(
    id, title, description, category, location,
    status, createdAt, viewCount, ownerId
) {
    override val type: ReportType = ReportType.SAFETY

    init {
        validateCategory()
    }

    override fun validateCategory() {
        require(category in ReportType.SAFETY.allowedCategories) {
            "Invalid safety report category."
        }
    }
}