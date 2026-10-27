// src/nl/brul/api/features/report/model/ReportStatus.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * De status van een melding: OPEN, IN_PROGRESS of RESOLVED.
 * Welke overgangen mogen, staat in ReportStatusRules.
 */
enum class ReportStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED
}
