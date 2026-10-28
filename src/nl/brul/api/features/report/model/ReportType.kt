// src/nl/brul/api/features/report/model/ReportType.kt
package nl.brul.api.features.report.model

/**
<<<<<<< Updated upstream
 * Laag: model (het domein).
 *
 * De drie soorten meldingen: SAFETY, NUISANCE en HELP_REQUEST.
 * Wordt opgeslagen in de kolom `type`, zodat de repository bij het teruglezen weet welk soort
 * object hij moet maken.
 */
enum class ReportType {
    SAFETY,
    NUISANCE,
    HELP_REQUEST
=======
 * The three kinds of reports, each with its own set of categories.
 *
 * The category lists live here once, so NewReport and the report subclasses
 * validate against the same source instead of keeping their own copies.
 */
enum class ReportType(
    val allowedCategories: Set<ReportCategory>
) {
    SAFETY(
        setOf(
            ReportCategory.THEFT,
            ReportCategory.SUSPICIOUS_SITUATION,
            ReportCategory.TRAFFIC_SAFETY
        )
    ),
    NUISANCE(
        setOf(
            ReportCategory.NOISE,
            ReportCategory.WASTE,
            ReportCategory.VANDALISM,
            ReportCategory.PARKING
        )
    ),
    HELP_REQUEST(
        setOf(
            ReportCategory.GROCERIES,
            ReportCategory.TRANSPORT,
            ReportCategory.MOVING,
            ReportCategory.CHORES,
            ReportCategory.OTHER
        )
    )
>>>>>>> Stashed changes
}
