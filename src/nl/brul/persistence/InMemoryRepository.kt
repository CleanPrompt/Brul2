package nl.brul.persistence

/**
 * Generic in-memory implementation of [CrudRepository].
 *
 * The repository does not know how to read the id of an entity, so the caller
 * passes that in as a function ([idOf]).
 *
 * Not thread-safe: intended for development, demos and tests.
 */
open class InMemoryRepository<T, ID>(
    private val idOf: (T) -> ID
) : CrudRepository<T, ID> {

    private val entities = mutableListOf<T>()

    override fun findAll(): List<T> =
        entities.toList()

    override fun findById(id: ID): T? =
        entities.find { entity -> idOf(entity) == id }

    override fun save(entity: T): T {
        entities.add(entity)

        return entity
    }

    override fun update(entity: T): T? {
        val index = entities.indexOfFirst { existing ->
            idOf(existing) == idOf(entity)
        }

        if (index == -1) {
            return null
        }

        entities[index] = entity

        return entity
    }

    override fun deleteById(id: ID): Boolean =
        entities.removeIf { entity -> idOf(entity) == id }
}
