// test/nl/brul/api/features/report/service/ReportStatusRulesTest.kt
package nl.brul.api.features.report.service

import kotlin.test.Test
import kotlin.test.assertFailsWith
import nl.brul.api.features.report.model.ReportStatus

class ReportStatusRulesTest {

    @Test
    fun `open report can move to in progress`() {
        ReportStatusRules.validateTransition(
            ReportStatus.OPEN,
            ReportStatus.IN_PROGRESS
        )
    }

    @Test
    fun `open report can be resolved directly`() {
        ReportStatusRules.validateTransition(
            ReportStatus.OPEN,
            ReportStatus.RESOLVED
        )
    }

    @Test
    fun `in progress report can be resolved`() {
        ReportStatusRules.validateTransition(
            ReportStatus.IN_PROGRESS,
            ReportStatus.RESOLVED
        )
    }

    @Test
    fun `resolved report cannot be reopened`() {
        assertFailsWith<IllegalArgumentException> {
            ReportStatusRules.validateTransition(
                ReportStatus.RESOLVED,
                ReportStatus.OPEN
            )
        }
    }

    @Test
    fun `in progress report cannot return to open`() {
        assertFailsWith<IllegalArgumentException> {
            ReportStatusRules.validateTransition(
                ReportStatus.IN_PROGRESS,
                ReportStatus.OPEN
            )
        }
    }

    @Test
    fun `keeping the same status is allowed`() {
        ReportStatusRules.validateTransition(
            ReportStatus.OPEN,
            ReportStatus.OPEN
        )

        ReportStatusRules.validateTransition(
            ReportStatus.RESOLVED,
            ReportStatus.RESOLVED
        )
    }
}