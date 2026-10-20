// src/nl/brul/api/features/report/service/ReportService.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.features.vote.model.VoteType
import nl.brul.api.features.vote.repository.VoteRepository

class ReportService(
    private val repository: ReportRepository,
    private val voteRepository: VoteRepository? = null
) {

    fun create(report: NewReport): Report =
        repository.create(report)

    fun findAll(): List<Report> =
        repository.findAll()

    fun findById(id: Long): Report? {
        require(id > 0) {
            "Report ID must be positive."
        }

        return repository.findById(id)
    }

    fun update(report: Report): Boolean =
        repository.update(report)

    fun delete(id: Long): Boolean {
        require(id > 0) {
            "Report ID must be positive."
        }

        // Vote cleanup is handled separately until deletion
        // can be made transactional across repositories.
        return repository.delete(id)
    }

    fun search(search: ReportSearch): List<Report> {
        var results = repository.findAll()

        val query = search.query?.trim()

        if (!query.isNullOrEmpty()) {
            results = results.filter { report ->
                report.title.contains(
                    query,
                    ignoreCase = true
                ) ||
                        report.description.contains(
                            query,
                            ignoreCase = true
                        )
            }
        }

        if (search.type != null) {
            results = results.filter { report ->
                report.type == search.type
            }
        }

        if (search.category != null) {
            results = results.filter { report ->
                report.category == search.category
            }
        }

        if (search.radiusKm != null) {
            val center = requireNotNull(search.location)

            results = results.filter { report ->
                report.location.distanceTo(center) <=
                        search.radiusKm
            }
        }

        return when (search.sort) {
            ReportSort.ID_ASC ->
                results.sortedBy { it.id }

            ReportSort.ID_DESC ->
                results.sortedByDescending { it.id }

            ReportSort.NEAREST -> {
                val center = requireNotNull(search.location)

                results.sortedWith(
                    compareBy<Report> {
                        it.location.distanceTo(center)
                    }.thenBy {
                        it.id
                    }
                )
            }

            ReportSort.NEWEST ->
                results.sortedWith(
                    compareByDescending<Report> {
                        it.createdAt
                    }.thenByDescending {
                        it.id
                    }
                )

            ReportSort.MOST_VIEWED ->
                results.sortedWith(
                    compareByDescending<Report> {
                        it.viewCount
                    }.thenByDescending {
                        it.createdAt
                    }.thenByDescending {
                        it.id
                    }
                )

            ReportSort.MOST_LIKED -> {
                val votes = requireNotNull(voteRepository) {
                    "Voting repository is required for MOST_LIKED sorting."
                }

                val likesByReport = results.associate { report ->
                    report.id to votes.findByReport(report.id)
                        .count { vote ->
                            vote.type == VoteType.LIKE
                        }
                }

                results.sortedWith(
                    compareByDescending<Report> { report ->
                        likesByReport.getValue(report.id)
                    }.thenByDescending {
                        it.createdAt
                    }.thenByDescending {
                        it.id
                    }
                )
            }
        }
    }
}