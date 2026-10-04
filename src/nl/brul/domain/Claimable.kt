package nl.brul.domain

interface Claimable {
    fun claim(user: User)
}