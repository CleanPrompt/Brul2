package nl.brul.persistence

import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import nl.brul.domain.HelpRequest
import nl.brul.domain.Location
import nl.brul.domain.NuisanceReport
import nl.brul.domain.NuisanceType
import nl.brul.domain.Report
import nl.brul.domain.Role
import nl.brul.domain.SafetyReport
import nl.brul.domain.Urgency
import nl.brul.domain.User

/**
 * Realistic demo data, so the API is not empty when the application starts.
 */
object ReportSeedData {

    val anna = User(id = "user-1", username = "anna", role = Role.USER)
    val bram = User(id = "user-2", username = "bram", role = Role.USER)
    val fatima = User(id = "user-3", username = "fatima", role = Role.USER)
    val admin = User(id = "admin-1", username = "admin", role = Role.ADMIN)

    fun reports(): List<Report> {
        val now = Clock.System.now()

        val brokenStreetlight = SafetyReport(
            title = "Broken streetlight on the Kerkstraat",
            description = "The streetlight near number 12 has been out for a week. The street is very dark at night.",
            photo = null,
            location = Location(latitude = 52.0712, longitude = 4.3001),
            createdBy = anna,
            urgency = Urgency.MEDIUM,
            createdAt = now - 6.days
        )

        val unsafeCrossing = SafetyReport(
            title = "Unsafe crossing near the primary school",
            description = "Cars drive too fast at the crossing during school hours.",
            photo = null,
            location = Location(latitude = 52.0698, longitude = 4.3045),
            createdBy = bram,
            urgency = Urgency.HIGH,
            createdAt = now - 3.days
        )

        val looseTile = SafetyReport(
            title = "Loose paving tile in front of the supermarket",
            description = null,
            photo = null,
            location = Location(latitude = 52.0731, longitude = 4.2989),
            createdBy = fatima,
            urgency = Urgency.LOW,
            createdAt = now - 10.days
        )

        val loudMusic = NuisanceReport(
            title = "Loud music every Friday night",
            description = "Music from the community garden until 2 AM.",
            photo = null,
            location = Location(latitude = 52.0705, longitude = 4.3020),
            createdBy = fatima,
            nuisanceType = NuisanceType.NOISE,
            createdAt = now - 2.days
        )

        val dumpedWaste = NuisanceReport(
            title = "Waste dumped next to the containers",
            description = "Bags of household waste and an old mattress next to the underground containers.",
            photo = null,
            location = Location(latitude = 52.0689, longitude = 4.3010),
            createdBy = anna,
            nuisanceType = NuisanceType.WASTE,
            createdAt = now - 1.days
        )

        val graffiti = NuisanceReport(
            title = "Graffiti on the playground fence",
            description = null,
            photo = null,
            location = Location(latitude = 52.0720, longitude = 4.3062),
            createdBy = bram,
            nuisanceType = NuisanceType.VANDALISM,
            createdAt = now - 12.hours
        )

        val groceries = HelpRequest(
            title = "Help carrying groceries",
            description = "I cannot lift heavy bags at the moment. Who can help me carry groceries on Saturday morning?",
            photo = null,
            location = Location(latitude = 52.0715, longitude = 4.3033),
            createdBy = anna,
            createdAt = now - 4.days
        )

        val gardenHelp = HelpRequest(
            title = "Help with the garden",
            description = "Looking for someone to help trim the hedge this weekend.",
            photo = null,
            location = Location(latitude = 52.0702, longitude = 4.2995),
            createdBy = bram,
            createdAt = now - 2.days
        )

        val walkingTheDog = HelpRequest(
            title = "Walking the dog this week",
            description = "I work late this week. Can someone walk my dog in the evening?",
            photo = null,
            location = Location(latitude = 52.0727, longitude = 4.3015),
            createdBy = fatima,
            createdAt = now - 5.hours
        )

        // Bring a few reports into a later status, through the domain methods
        // so that the business rules also apply to the demo data.
        gardenHelp.claim(fatima)
        looseTile.resolve(resolvedBy = fatima)

        return listOf(
            brokenStreetlight,
            unsafeCrossing,
            looseTile,
            loudMusic,
            dumpedWaste,
            graffiti,
            groceries,
            gardenHelp,
            walkingTheDog
        )
    }
}
