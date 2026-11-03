// src/nl/brul/api/features/report/ai/ReportAssistantAi.kt
package nl.brul.api.features.report.ai

import nl.brul.api.features.report.model.DuplicateCandidate
import nl.brul.api.features.report.model.DuplicateQuery
import nl.brul.api.features.report.model.ReportSuggestion

/**
 * Laag: ai (de koppeling met een AI-dienst).
 *
 * Interface: wat de AI voor het melden moet kunnen, zonder te zeggen welke AI het is.
 * Net als bij ReportRepository en PhotoStorage kent de service alleen deze interface
 * (Dependency Inversion). Daardoor:
 * - test je de service met een nep-AI die een vast antwoord geeft, zonder internet en gratis;
 * - kun je van AI-aanbieder wisselen zonder de service aan te passen.
 * De echte implementatie is ClaudeReportAssistantAi.
 *
 * Beide functies geven `null` als de AI geen bruikbaar antwoord heeft. De service valt dan terug
 * op de vaste regels (KeywordClassifier en DuplicateRules), zodat de app altijd blijft werken.
 */
interface ReportAssistantAi {

    /** S1: stel een type en categorie voor op basis van de tekst van de gebruiker. */
    fun suggestCategory(title: String, description: String): ReportSuggestion?

    /**
     * D6: welke van de kandidaten gaan echt over dezelfde situatie?
     * Geeft de id's van die meldingen; de service gebruikt alleen id's die in [candidates]
     * staan, dus de AI kan geen meldingen verzinnen.
     */
    fun pickDuplicates(query: DuplicateQuery, candidates: List<DuplicateCandidate>): Set<Long>?
}
