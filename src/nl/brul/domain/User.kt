package nl.brul.domain

data class User(
    val id: String,
    val username: String,
    val role: Role
) {
    val isAdmin: Boolean
        get() = role == Role.ADMIN
}
