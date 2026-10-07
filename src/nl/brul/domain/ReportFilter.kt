package nl.brul.domain

/**
 * Search and filter criteria for a collection of reports.
 *
 * Every criterion that is set becomes a predicate `(Report) -> Boolean`;
 * a report matches when it satisfies all of them. Criteria that are not set
 * are simply left out, so an empty filter matches every report.
 */
data class ReportFilter(
    val search: String? = null,
    val type: ReportType? = null,
    val status: ReportStatus? = null,
    val urgency: Urgency? = null,
    val nuisanceType: NuisanceType? = null
) {

    private val predicates: List<(Report) -> Boolean> =
        listOfNotNull(
            search
                ?.takeIf { term -> term.isNotBlank() }
                ?.let { term -> { report: Report -> report.matchesSearch(term) } },

            type?.let { wantedType -> { report: Report -> report.type == wantedType } },

            status?.let { wantedStatus -> { report: Report -> report.status == wantedStatus } },

            urgency?.let { wantedUrgency ->
                { report: Report -> report is SafetyReport && report.urgency == wantedUrgency }
            },

            nuisanceType?.let { wantedNuisanceType ->
                { report: Report -> report is NuisanceReport && report.nuisanceType == wantedNuisanceType }
            }
        )

    fun matches(report: Report): Boolean =
        predicates.all { predicate -> predicate(report) }
}

/**
 * Extension function on any collection of reports, so filtering reads as
 * `reports.filterBy(filter)`.
 */
fun Iterable<Report>.filterBy(criteria: ReportFilter): List<Report> =
    filter { report -> criteria.matches(report) }
