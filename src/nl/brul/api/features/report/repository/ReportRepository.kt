// src/nl/brul/api/features/report/repository/ReportRepository.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.Report

/**
 * Laag: repository (het geheugen).
 *
 * Interface: alleen de lijst met functies om meldingen op te slaan en op te halen, zonder echte
 * code.
 * ReportService kent alleen deze interface, niet de echte database.
 * Daardoor kun je in tests een nep-repository (fake) meegeven, en kan de database vervangen worden
 * zonder de service aan te passen: Dependency Inversion uit SOLID.
 * De echte implementatie is ExposedReportRepository.
 */
interface ReportRepository {

    fun create(report: NewReport): Report

    fun findAll(): List<Report>

    fun findById(id: Long): Report?

    // Default implementation keeps existing test repositories compatible.
    fun findByIdAndIncrementViews(id: Long): Report? = findById(id)

    fun update(report: Report): Boolean

    fun delete(id: Long): Boolean
}