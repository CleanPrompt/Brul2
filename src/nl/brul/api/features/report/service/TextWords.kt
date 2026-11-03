// src/nl/brul/api/features/report/service/TextWords.kt
package nl.brul.api.features.report.service

/**
 * Laag: service (de regels).
 *
 * Maakt van een stuk tekst een lijst losse woorden: alles in kleine letters, leestekens weg,
 * en "é" wordt "e". Gebruikt door KeywordClassifier en DuplicateRules, zodat beide de tekst op
 * precies dezelfde manier lezen.
 * `object`: er bestaat er precies één van.
 */
object TextWords {

    /** Woorden die bijna overal in staan en dus niets zeggen over de situatie. */
    val STOP_WORDS: Set<String> = setOf(
        "de", "het", "een", "en", "of", "van", "in", "op", "aan", "bij", "met", "voor", "naar",
        "door", "dat", "die", "dit", "deze", "er", "is", "zijn", "was", "ik", "je", "jij", "we",
        "wij", "ze", "zij", "hij", "niet", "wel", "al", "ook", "maar", "om", "te", "tot", "uit",
        "over", "als", "dan", "heel", "erg", "mijn", "onze", "ons", "hier", "daar", "weer",
        "staat", "staan", "heeft", "hebben", "wordt", "worden", "the", "a", "an", "and", "is",
        "are", "of", "on", "at", "to"
    )

    fun of(text: String): List<String> =
        java.text.Normalizer.normalize(text.lowercase(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .split(Regex("[^a-z0-9]+"))
            .filter { it.isNotBlank() }

    /**
     * De woorden die iets zeggen: zonder stopwoorden, minstens 3 letters, en ingekort tot de
     * eerste 5 letters. Zo tellen "gestolen" en "gestolene", of "fiets" en "fietsen", als hetzelfde
     * woord.
     */
    fun meaningful(text: String): Set<String> =
        of(text)
            .filter { it.length >= 3 && it !in STOP_WORDS }
            .map { it.take(5) }
            .toSet()
}
