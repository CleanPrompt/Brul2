package nl.brul.domain

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A report about nuisance in the neighbourhood, with a [NuisanceType].
 */
class NuisanceReport(
    title: String,
    description: String?,
    photo: String?,
    location: Location,
    createdBy: User,
    val nuisanceType: NuisanceType,
    createdAt: Instant = Clock.System.now()
) : Report(
    title = title,
    description = description,
    photo = photo,
    location = location,
    createdBy = createdBy,
    createdAt = createdAt
) {

    override val type: ReportType = ReportType.NUISANCE
}
