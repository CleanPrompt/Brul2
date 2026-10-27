// src/nl/brul/api/features/report/model/ReportType.kt
package nl.brul.api.features.report.model

/**
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
}
