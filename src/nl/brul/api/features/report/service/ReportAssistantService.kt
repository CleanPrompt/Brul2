// src/nl/brul/api/features/report/service/ReportAssistantService.kt
package nl.brul.api.features.report.service

import java.time.Clock
import nl.brul.api.features.report.ai.ReportAssistantAi
import nl.brul.api.features.report.model.DuplicateCandidate
import nl.brul.api.features.report.model.DuplicateQuery
import nl.brul.api.features.report.model.ReportSuggestion
import nl.brul.api.features.report.repository.ReportRepository

/**
 * Laag: service (de regels).
 *
 * De "meldassistent": helpt een bewoner terwijl hij een melding maakt, nog vóór het opslaan.
 *
 * 1. `suggest`: stelt een type en categorie voor (S1). Eerst de AI; geeft die niets of gaat er
 *    iets mis, dan de vaste trefwoorden (S3).
 * 2. `findDuplicates`: zoekt bestaande meldingen over dezelfde situatie. DuplicateRules kiest
 *    eerst de meldingen in de buurt (D1 t/m D4); daarna kiest de AI de echte dubbele (D6), of
 *    zonder AI de woordvergelijking (D5).
 *
 * De AI is optioneel (`ai = null`): zonder API-sleutel werkt alles nog, alleen minder slim.
 * Een fout van de AI wordt nooit een fout voor de gebruiker; `runCatching` vangt hem op.
 * Krijgt zijn hulpjes via de constructor (dependency injection), net als ReportService.
 */
class ReportAssistantService(
    private val repository: ReportRepository,
    private val ai: ReportAssistantAi? = null,
    private val clock: Clock = Clock.systemUTC()
) {

    /** S1 en S3: een voorstel, of `null` als zelfs de trefwoorden niets herkennen. */
    fun suggest(title: String, description: String): ReportSuggestion? {
        require(title.isNotBlank() || description.isNotBlank()) {
            "A title or description is needed for a suggestion."
        }

        require(title.length <= MAX_TEXT_LENGTH && description.length <= MAX_TEXT_LENGTH) {
            "Text is too long for a suggestion."
        }

        val fromAi = ai?.let { assistant ->
            runCatching { assistant.suggestCategory(title, description) }.getOrNull()
        }

        return fromAi ?: KeywordClassifier.suggest(title, description)
    }

    /** D1 t/m D6: de bestaande meldingen die waarschijnlijk over hetzelfde gaan, hoogstens 5. */
    fun findDuplicates(query: DuplicateQuery): List<DuplicateCandidate> {
        require(query.text.length <= 2 * MAX_TEXT_LENGTH) {
            "Text is too long to look for duplicates."
        }

        val candidates = DuplicateRules.candidates(
            query = query,
            reports = repository.findAll(),
            now = clock.instant()
        ).take(MAX_CANDIDATES_FOR_AI)

        if (candidates.isEmpty()) {
            return emptyList()
        }

        val chosenByAi = ai?.let { assistant ->
            runCatching { assistant.pickDuplicates(query, candidates) }.getOrNull()
        }

        val duplicates = if (chosenByAi != null) {
            // D6: keep the order of the rules, and only ids from the candidate list.
            candidates.filter { it.report.id in chosenByAi }
        } else {
            // D5
            candidates.filter { DuplicateRules.isLikelyDuplicate(query, it) }
        }

        return duplicates.take(DuplicateRules.MAX_RESULTS)
    }

    companion object {
        /** Langer dan dit is geen normale melding; zo blijft ook de vraag aan de AI klein. */
        const val MAX_TEXT_LENGTH = 2_000

        /** Hoeveel meldingen in de buurt de AI maximaal te zien krijgt. */
        const val MAX_CANDIDATES_FOR_AI = 10
    }
}
