// src/nl/brul/api/features/report/repository/FileSystemPhotoStorage.kt
package nl.brul.api.features.report.repository

import java.nio.file.Files
import java.nio.file.Path

/**
 * Laag: repository (het geheugen).
 *
 * Bewaart elke foto als bestand in de map [root] (in Routing.kt: `uploads`, niet in Git).
 * De sleutels maakt de service zelf (een UUID), maar voor de zekerheid weigert [resolve] alles
 * wat buiten de map zou kunnen wijzen, zoals `../`.
 */
class FileSystemPhotoStorage(
    private val root: Path
) : PhotoStorage {

    override fun save(key: String, bytes: ByteArray) {
        Files.createDirectories(root)
        Files.write(resolve(key), bytes)
    }

    override fun load(key: String): ByteArray? {
        val file = resolve(key)
        return if (Files.exists(file)) Files.readAllBytes(file) else null
    }

    override fun delete(key: String) {
        Files.deleteIfExists(resolve(key))
    }

    private fun resolve(key: String): Path {
        require(key.matches(Regex("[A-Za-z0-9._-]+")) && !key.contains("..")) {
            "Invalid storage key."
        }

        return root.resolve(key)
    }
}
