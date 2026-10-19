// src/nl/brul/api/features/report/repository/ReportRepository.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.Report

interface ReportRepository {

    fun create(report: NewReport): Report

    fun findAll(): List<Report>

    fun findById(id: Long): Report?

    fun update(report: Report): Boolean

    fun delete(id: Long): Boolean
}