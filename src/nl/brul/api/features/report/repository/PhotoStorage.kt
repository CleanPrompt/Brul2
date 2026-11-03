// src/nl/brul/api/features/report/repository/PhotoStorage.kt
package nl.brul.api.features.report.repository

/**
 * Laag: repository (het geheugen).
 *
 * Interface: waar de bytes van foto's bewaard worden. De database slaat alleen de sleutel op
 * (`storageKey`). Net als ReportRepository kent de service alleen deze interface, zodat tests een
 * versie in het geheugen kunnen gebruiken en je later bijvoorbeeld opslag in de cloud kunt
 * aansluiten zonder de service te veranderen.
 * De echte implementatie is FileSystemPhotoStorage.
 */
interface PhotoStorage {

    fun save(key: String, bytes: ByteArray)

    fun load(key: String): ByteArray?

    fun delete(key: String)
}
