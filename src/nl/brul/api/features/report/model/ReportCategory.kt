// src/nl/brul/api/features/report/model/ReportCategory.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * Enum met alle 12 categorieën. Welke categorie bij welk type mag, staat in de subklassen en in
 * NewReport.
 * Een enum voorkomt tikfouten: een waarde die niet in de lijst staat, bestaat niet.
 */
enum class ReportCategory {
    THEFT,
    SUSPICIOUS_SITUATION,
    TRAFFIC_SAFETY,
    NOISE,
    WASTE,
    VANDALISM,
    PARKING,
    GROCERIES,
    TRANSPORT,
    MOVING,
    CHORES,
    OTHER
}