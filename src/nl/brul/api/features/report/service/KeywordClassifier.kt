// src/nl/brul/api/features/report/service/KeywordClassifier.kt
package nl.brul.api.features.report.service

import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportSuggestion
import nl.brul.api.features.report.model.ReportType

/**
 * Laag: service (de regels).
 *
 * S3: het reserveplan als de AI er niet is (geen API-sleutel, geen internet of een fout).
 * Zoekt naar vaste trefwoorden in de titel en beschrijving en kiest de categorie met de meeste
 * treffers. Minder slim dan de AI ("rijwiel weggenomen" herkent hij niet als diefstal), maar
 * altijd beschikbaar, gratis en voorspelbaar te testen.
 *
 * Een trefwoord is een woordbegin: "gestol" vindt "gestolen", "parkeer" vindt "parkeerplaats".
 * Een pure functie: dezelfde tekst geeft altijd hetzelfde voorstel.
 */
object KeywordClassifier {

    /** Trefwoorden per categorie, in het Nederlands en een paar in het Engels. */
    private val KEYWORDS: Map<ReportCategory, List<String>> = mapOf(
        ReportCategory.THEFT to listOf(
            "gestol", "stelen", "steelt", "diefstal", "dief", "inbra", "ingebrok", "inbrek",
            "beroofd", "weggenom", "gejat", "stolen", "theft", "burglar"
        ),
        ReportCategory.SUSPICIOUS_SITUATION to listOf(
            "verdacht", "rondhang", "gluur", "loert", "onbekend", "vreemde", "suspicious"
        ),
        ReportCategory.TRAFFIC_SAFETY to listOf(
            "verkeer", "hardrijd", "snelheid", "oversteek", "zebrapad", "kruispunt", "drempel",
            "fietspad", "stoplicht", "verkeerslicht", "traffic", "speeding"
        ),
        ReportCategory.NOISE to listOf(
            "lawaai", "geluid", "herrie", "muziek", "blaf", "geschreeuw", "schreeuw", "noise",
            "loud"
        ),
        ReportCategory.WASTE to listOf(
            "afval", "vuilnis", "zwerfvuil", "rommel", "troep", "gedumpt", "dump", "grofvuil",
            "container", "waste", "trash", "litter"
        ),
        ReportCategory.VANDALISM to listOf(
            "vernield", "vernieling", "graffiti", "beklad", "kapotgemaakt", "ingegooid",
            "beschadigd", "vandal"
        ),
        ReportCategory.PARKING to listOf(
            "geparkeerd", "parkeer", "parkeren", "foutparkeer", "oprit", "invalidenplek",
            "parking", "parked"
        ),
        ReportCategory.GROCERIES to listOf(
            "boodschap", "supermarkt", "winkel", "groceries", "shopping"
        ),
        ReportCategory.TRANSPORT to listOf(
            "vervoer", "ziekenhuis", "huisarts", "afspraak", "brengen", "ophalen", "lift",
            "transport", "ride"
        ),
        ReportCategory.MOVING to listOf(
            "verhuiz", "sjouw", "tillen", "meubel", "kast", "moving"
        ),
        ReportCategory.CHORES to listOf(
            "klus", "tuin", "gras", "maaien", "schoonma", "schilder", "ophangen", "repare",
            "sneeuw", "chores"
        )
    )

    /** Woorden die bij een veiligheidsmelding wijzen op direct gevaar. */
    private val URGENT_WORDS = listOf(
        "nu", "nog", "momenteel", "spoed", "gevaar", "gevaarlijk", "brand", "bezig"
    )

    /** S1 en S3: een voorstel, of `null` als er geen enkel trefwoord in de tekst staat. */
    fun suggest(title: String, description: String): ReportSuggestion? {
        val words = TextWords.of("$title $description")

        val (category, hits) = KEYWORDS
            .map { (category, keywords) ->
                category to keywords.count { keyword ->
                    words.any { word -> word.startsWith(keyword) }
                }
            }
            .maxByOrNull { (_, hits) -> hits }
            ?: return null

        if (hits == 0) {
            return null
        }

        val type = ReportType.entries.first { category in it.allowedCategories }

        val urgent = if (type == ReportType.SAFETY) {
            words.any { it in URGENT_WORDS }
        } else {
            null
        }

        return ReportSuggestion(type, category, urgent, ReportSuggestion.Source.KEYWORDS)
    }
}
