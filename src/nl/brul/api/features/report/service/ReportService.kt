// src/nl/brul/api/features/report/service/ReportService.kt
package nl.brul.api.features.report.service

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import nl.brul.api.common.error.DomainConflictException
import nl.brul.api.features.help.repository.HelpParticipationRepository
import nl.brul.api.features.help.service.HelpParticipationService
import nl.brul.api.features.report.model.HelpRequest
import nl.brul.api.features.report.model.NewReport
import nl.brul.api.features.report.model.NuisanceReport
import nl.brul.api.features.report.model.Report
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.repository.ReportRepository
import nl.brul.api.features.vote.model.VoteType
import nl.brul.api.features.vote.repository.VoteRepository

/**
 * Laag: service (de regels).
 *
 * Bevat de bedrijfslogica van meldingen en zit tussen de routes en de repository.
 * Krijgt zijn hulpjes via de constructor (dependency injection); in tests geef je
 * nep-repositories mee en laat je weg wat je niet nodig hebt.
 * - `voteRepository`: sorteren op MOST_LIKED.
 * - `helpParticipationRepository`: de capaciteitsregel (R10).
 * - `clock`: de huidige tijd; tests geven een vaste klok mee.
 * - `participationService`: de wachtlijst laten doorschuiven en inschrijvingen annuleren nadat
 *   een hulpvraag is gewijzigd (R8, R10, R13).
 * - `photoService`: fotobestanden opruimen met een melding (F7) en de regel dat een
 *   overlastmelding een resultaatfoto nodig heeft (F8).
 * In Routing.kt worden ze allemaal meegegeven.
 */
class ReportService(
    private val repository: ReportRepository,
    private val voteRepository: VoteRepository? = null,
    private val helpParticipationRepository: HelpParticipationRepository? = null,
    private val clock: Clock = Clock.systemUTC(),
    private val participationService: HelpParticipationService? = null,
    private val photoService: ReportPhotoService? = null
) {

    fun create(report: NewReport): Report {
        // R1: a new help request must be planned in the future.
        report.slot?.let { slot ->
            require(!slot.hasStarted(now())) {
                "A help request must start in the future."
            }
        }

        return repository.create(report)
    }

    fun findAll(): List<Report> =
        repository.findAll()

    fun findById(id: Long): Report? {
        require(id > 0) {
            "Report ID must be positive."
        }

        return repository.findById(id)
    }

    fun findByIdAndIncrementViews(id: Long): Report? {
        require(id > 0) {
            "Report ID must be positive."
        }

        return repository.findByIdAndIncrementViews(id)
    }

    /**
     * Wijzigt een melding, maar pas na een aantal controles:
     *
     * 1. De statusovergang moet mogen (ReportStatusRules); een hulpvraag kan alleen naar
     *    RESOLVED (R14).
     * 2. Bij een HelpRequest mag `volunteersNeeded` niet lager zijn dan het aantal deelnemers
     *    (R10), en ligt het tijdslot vast zodra de hulpvraag begonnen is of er iemand is
     *    ingeschreven (R12). Zie HelpRequestUpdateRules.
     * 3. Een NuisanceReport mag pas op RESOLVED als er een resultaatfoto is (F8).
     *
     * Na het opslaan van een hulpvraag schuift de wachtlijst door of worden inschrijvingen
     * geannuleerd (participationService).
     *
     * `report is HelpRequest` is een type check; daarna weet Kotlin dat het een HelpRequest is
     * (smart cast).
     * Geeft false als de melding niet bestaat.
     */
    fun update(report: Report): Boolean {
        val existing = repository.findById(report.id)
            ?: return false

        ReportStatusRules.validateTransition(
            current = existing.status,
            next = report.status,
            type = report.type
        )

        if (report is HelpRequest && existing is HelpRequest) {
            val signedUpCount = helpParticipationRepository
                ?.countSignedUp(report.id)
                ?: 0

            // R10 and R12
            HelpRequestUpdateRules.validate(
                existing = existing,
                updated = report,
                signedUpCount = signedUpCount,
                now = now()
            )
        }

        // F8: a nuisance report is only solved when a result photo shows it.
        if (
            report is NuisanceReport &&
            report.status == ReportStatus.RESOLVED &&
            existing.status != ReportStatus.RESOLVED &&
            photoService != null &&
            !photoService.hasResultPhoto(report.id)
        ) {
            throw DomainConflictException(
                code = "RESULT_PHOTO_REQUIRED",
                message = "Add a result photo before resolving this nuisance report."
            )
        }

        val updated = repository.update(report)

        if (updated && report is HelpRequest) {
            // R13 (closing cancels everyone) or R10 (extra places promote the waitlist).
            participationService?.afterHelpRequestUpdated(report.id)
        }

        return updated
    }

    /**
     * Verwijdert een melding. Stemmen, deelnames en foto-rijen verdwijnen automatisch via de
     * database (CASCADE op de foreign keys).
     * De fotobestanden op schijf kent de database niet; die ruimt deze functie zelf op (F7).
     */
    fun delete(id: Long): Boolean {
        require(id > 0) {
            "Report ID must be positive."
        }

        // Votes, participations and photo rows are removed automatically
        // by database foreign key cascades. Photo files are not (F7).
        val photoKeys = photoService?.storageKeysOf(id).orEmpty()

        val deleted = repository.delete(id)

        if (deleted) {
            photoService?.deleteFiles(photoKeys)
        }

        return deleted
    }

    /**
     * Zoekt, filtert en sorteert meldingen: functioneel programmeren met `filter` en `sortedWith`.
     * Begint met alle meldingen en filtert daarna op zoekwoord (titel of beschrijving), type,
     * categorie en afstand.
     * `thenBy` is de tiebreaker, zodat de volgorde altijd voorspelbaar is.
     * Let op: dit gebeurt in het geheugen. Bij veel meldingen zou filteren in de database (WHERE)
     * sneller zijn.
     */
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

    private fun now(): Instant =
        clock.instant().truncatedTo(ChronoUnit.MILLIS)
}
