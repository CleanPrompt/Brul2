package nl.brul.domain

import kotlinx.serialization.Serializable

@Serializable
enum class ReportStatus {
    OPEN,
    CLAIMED,
    RESOLVED
}