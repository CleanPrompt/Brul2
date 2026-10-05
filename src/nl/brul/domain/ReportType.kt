package nl.brul.domain

import kotlinx.serialization.Serializable

@Serializable
enum class ReportType {
    SAFETY,
    NUISANCE,
    HELP
}