// src/nl/brul/api/features/report/service/ReportStatusRules.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.ReportStatus

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