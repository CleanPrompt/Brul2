// src/nl/brul/api/features/report/service/ReportStatusRules.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType

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
 *
 * Voor een hulpvraag geldt nog een extra regel (R14): zie de tweede `validateTransition`.
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

    /**
     * R14: the same rules, plus one extra for help requests.
     * A help request is "in progress" because its time slot has started
     * (see HelpRequestPhase), so the owner can only close it (RESOLVED).
     */
    fun validateTransition(
        current: ReportStatus,
        next: ReportStatus,
        type: ReportType
    ) {
        validateTransition(current, next)

        if (type == ReportType.HELP_REQUEST && current != next) {
            require(next == ReportStatus.RESOLVED) {
                "A help request can only be closed; its progress follows from its time slot."
            }
        }
    }
}
