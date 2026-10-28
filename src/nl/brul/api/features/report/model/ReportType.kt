// src/nl/brul/api/features/report/model/ReportType.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * De drie soorten meldingen: SAFETY, NUISANCE en HELP_REQUEST.
 * Wordt opgeslagen in de kolom `type`, zodat de repository bij het teruglezen weet welk soort
 * object hij moet maken.
 *
 * Elk type kent ook zijn toegestane categorieën (`allowedCategories`). Die lijst staat hier één
 * keer, zodat NewReport en de subklassen tegen dezelfde bron controleren in plaats van elk een
 * eigen kopie bij te houden.
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
}
