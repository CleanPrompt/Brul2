// src/nl/brul/api/features/report/service/ReportStatusRules.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.ReportStatus

/**
 * Laag: service (domeinregels voor de status).
 *
 * `object`: er bestaat er precies één van; je hoeft er niets van te maken.
 * `validateTransition` werkt als een simpele toestandsmachine:
 *
 * - OPEN mag naar IN_PROGRESS of RESOLVED
 * - IN_PROGRESS mag alleen naar RESOLVED
 * - RESOLVED is het eindstation
 * - dezelfde status houden mag altijd
 *
 * Mag het niet, dan gooit `require` een IllegalArgumentException, en dat wordt 400 Bad Request.
 */
object ReportStatusRules {

    fun validateTransition(
        current: ReportStatus,
        next: ReportStatus
    ) {
        if (current == next) {
            return
        }

        val allowed = when (current) {
            ReportStatus.OPEN ->
                next == ReportStatus.IN_PROGRESS ||
                        next == ReportStatus.RESOLVED

            ReportStatus.IN_PROGRESS ->
                next == ReportStatus.RESOLVED

            ReportStatus.RESOLVED -> false
        }

        require(allowed) {
            "Cannot change report status from $current to $next."
        }
    }
}