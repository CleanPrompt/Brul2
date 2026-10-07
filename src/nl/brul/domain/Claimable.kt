package nl.brul.domain

/**
 * A report that another resident can take on ("claim").
 */
interface Claimable {

    val claimedBy: User?

    fun claim(user: User)
}
