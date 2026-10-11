package nl.brul.persistence

import nl.brul.domain.Report

/**
 * Storage for reports. The rest of the application only depends on this
 * interface, so the in-memory implementation can later be replaced by a
 * database implementation (for example with Exposed) without changing the
 * service or the routes.
 */
interface ReportRepository : CrudRepository<Report, String>
