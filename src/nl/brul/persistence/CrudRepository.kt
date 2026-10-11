package nl.brul.persistence

/**
 * Generic CRUD operations for an entity of type [T] with an identifier of type [ID].
 */
interface CrudRepository<T, ID> {

    fun findAll(): List<T>

    fun findById(id: ID): T?

    fun save(entity: T): T

    fun update(entity: T): T?

    fun deleteById(id: ID): Boolean
}
