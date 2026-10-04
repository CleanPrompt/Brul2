package nl.brul.persistence

import nl.brul.domain.Report

interface ReportRepository {

    fun findAll(): List<Report>

    fun findById(id: String): Report?

    fun save(report: Report): Report

    fun update(report: Report): Report?

    fun deleteById(id: String): Boolean
}