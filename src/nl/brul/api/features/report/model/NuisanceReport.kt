// src/nl/brul/api/features/report/model/NuisanceReport.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * Overlastmelding, erft van Report.
 * Toegestane categorieën: NOISE, WASTE, VANDALISM en PARKING.
 * Geen extra veld.
 * Mag pas naar RESOLVED als er een resultaatfoto bij zit (F8, gecontroleerd in ReportService).
 */
class NuisanceReport(
    id: Long,
    title: String,
    description: String,
    category: ReportCategory,
    location: Location,
    status: ReportStatus = ReportStatus.OPEN,
    createdAt: Long = System.currentTimeMillis(),
    viewCount: Int = 0,
    ownerId: Long? = null
) : Report(
    id, title, description, category, location,
    status, createdAt, viewCount, ownerId
) {
    override val type: ReportType = ReportType.NUISANCE

    init {
        validateCategory()
    }

    override fun validateCategory() {
        require(category in ReportType.NUISANCE.allowedCategories) {
            "Invalid nuisance report category."
        }
    }
}