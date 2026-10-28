// src/nl/brul/api/database/DatabaseSeeder.kt
package nl.brul.api.database

import nl.brul.api.features.help.model.ParticipationStatus
import nl.brul.api.features.help.repository.HelpParticipationsTable
import nl.brul.api.features.report.repository.ReportPhotosTable
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportStatus
import nl.brul.api.features.report.model.ReportType
import nl.brul.api.features.report.repository.ReportsTable
import nl.brul.api.features.user.model.UserRole
import nl.brul.api.features.user.repository.UsersTable
import nl.brul.api.features.vote.model.VoteType
import nl.brul.api.features.vote.repository.VotesTable
import nl.brul.api.security.BCryptPasswordHasher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseSeeder {

    private val logger = LoggerFactory.getLogger(DatabaseSeeder::class.java)
    private val passwordHasher = BCryptPasswordHasher(workFactor = 10)

    /**
     * Seeds the database with a comprehensive test dataset covering all user roles,
     * report types, categories, participation states, votes, and geographical locations.
     *
     * @param database The Exposed Database instance.
     * @param force If true, existing records will be cleared before seeding. If false,
     *              seeding only occurs when the users table is completely empty.
     */
    fun seed(database: Database, force: Boolean = false) {
        transaction(database) {
            val userCount = UsersTable.selectAll().count()
            if (!force && userCount > 0L) {
                logger.info("Database already contains data ($userCount users found). Seeding skipped.")
                return@transaction
            }

            if (force) {
                logger.info("Forced seeding enabled: clearing existing data...")
                HelpParticipationsTable.deleteAll()
                ReportPhotosTable.deleteAll()
                VotesTable.deleteAll()
                ReportsTable.deleteAll()
                UsersTable.deleteAll()
            }

            logger.info("Seeding Brul2 database with comprehensive test data...")

            val now = System.currentTimeMillis()
            val hour = 3_600_000L
            val day = 86_400_000L

            // Help request slots start on a whole hour, relative to today,
            // so the demo data always has upcoming help requests.
            val startOfHour = now - now % hour

            // -------------------------------------------------------------
            // 1. Users (Admin + Residents)
            // -------------------------------------------------------------
            val adminPasswordHash = passwordHasher.hash("AdminPassword123!")
            val userPasswordHash = passwordHasher.hash("Password123!")

            val adminId = UsersTable.insert {
                it[username] = "administrator"
                it[email] = "admin@example.com"
                it[passwordHash] = adminPasswordHash
                it[createdAt] = now - 7 * day
                it[role] = UserRole.ADMIN
            } get UsersTable.id

            val anastasiaId = UsersTable.insert {
                it[username] = "anastasia"
                it[email] = "anastasia@example.com"
                it[passwordHash] = userPasswordHash
                it[createdAt] = now - 6 * day
                it[role] = UserRole.USER
            } get UsersTable.id

            val bobId = UsersTable.insert {
                it[username] = "bob"
                it[email] = "bob@example.com"
                it[passwordHash] = userPasswordHash
                it[createdAt] = now - 5 * day
                it[role] = UserRole.USER
            } get UsersTable.id

            val claraId = UsersTable.insert {
                it[username] = "clara"
                it[email] = "clara@example.com"
                it[passwordHash] = userPasswordHash
                it[createdAt] = now - 4 * day
                it[role] = UserRole.USER
            } get UsersTable.id

            val davidId = UsersTable.insert {
                it[username] = "david"
                it[email] = "david@example.com"
                it[passwordHash] = userPasswordHash
                it[createdAt] = now - 3 * day
                it[role] = UserRole.USER
            } get UsersTable.id

            val evaId = UsersTable.insert {
                it[username] = "eva"
                it[email] = "eva@example.com"
                it[passwordHash] = userPasswordHash
                it[createdAt] = now - 2 * day
                it[role] = UserRole.USER
            } get UsersTable.id

            // -------------------------------------------------------------
            // 2. Reports (All Types, All Categories, Staggered Locations)
            // Center of Breda / Avans: ~51.585, 4.775
            // -------------------------------------------------------------

            // Report 1: Safety - SUSPICIOUS_SITUATION (Urgent, Open) - Breda Centrum
            val report1Id = ReportsTable.insert {
                it[type] = ReportType.SAFETY.name
                it[ownerId] = anastasiaId
                it[title] = "Verdachte situatie bij fietsenstalling"
                it[description] = "Twee personen kijken verdacht naar geparkeerde scooters en fietsen."
                it[category] = ReportCategory.SUSPICIOUS_SITUATION.name
                it[latitude] = 51.585
                it[longitude] = 4.775
                it[status] = ReportStatus.OPEN.name
                it[urgent] = true
                it[volunteersNeeded] = null
                it[createdAt] = now - 3 * hour
                it[viewCount] = 28
            } get ReportsTable.id

            // Report 2: Safety - TRAFFIC_SAFETY (Non-urgent, In Progress) - Lovensdijkstraat
            val report2Id = ReportsTable.insert {
                it[type] = ReportType.SAFETY.name
                it[ownerId] = bobId
                it[title] = "Gevaarlijk kruispunt Lovensdijkstraat"
                it[description] = "Verkeerslichten knipperen oranje, auto's verlenen geen voorrang aan fietsende studenten."
                it[category] = ReportCategory.TRAFFIC_SAFETY.name
                it[latitude] = 51.583
                it[longitude] = 4.792
                it[status] = ReportStatus.IN_PROGRESS.name
                it[urgent] = false
                it[volunteersNeeded] = null
                it[createdAt] = now - 1 * day
                it[viewCount] = 45
            } get ReportsTable.id

            // Report 3: Safety - THEFT (Urgent, Resolved) - Station Breda
            val report3Id = ReportsTable.insert {
                it[type] = ReportType.SAFETY.name
                it[ownerId] = claraId
                it[title] = "Fietsendiefstal bij station Breda"
                it[description] = "Blauwe Gazelle stadsfiets ontvreemd tussen 14:00 en 17:00 bij de noordzijde van het station."
                it[category] = ReportCategory.THEFT.name
                it[latitude] = 51.595
                it[longitude] = 4.780
                it[status] = ReportStatus.RESOLVED.name
                it[urgent] = true
                it[volunteersNeeded] = null
                it[createdAt] = now - 3 * day
                it[viewCount] = 82
            } get ReportsTable.id

            // Report 4: Nuisance - NOISE (Open) - Hogeschoollaan
            val report4Id = ReportsTable.insert {
                it[type] = ReportType.NUISANCE.name
                it[ownerId] = bobId
                it[title] = "Geluidsoverlast late uurtjes studentenflat"
                it[description] = "Hard draaiende muziek en lawaai op het balkon na middernacht."
                it[category] = ReportCategory.NOISE.name
                it[latitude] = 51.588
                it[longitude] = 4.785
                it[status] = ReportStatus.OPEN.name
                it[urgent] = null
                it[volunteersNeeded] = null
                it[createdAt] = now - 5 * hour
                it[viewCount] = 16
            } get ReportsTable.id

            // Report 5: Nuisance - WASTE (Open) - Valkenbergpark
            val report5Id = ReportsTable.insert {
                it[type] = ReportType.NUISANCE.name
                it[ownerId] = anastasiaId
                it[title] = "Gedumpt grofvuil in het Valkenbergpark"
                it[description] = "Oude matras en kapotte meubels achtergelaten naast de openbare afvalbakken."
                it[category] = ReportCategory.WASTE.name
                it[latitude] = 51.591
                it[longitude] = 4.778
                it[status] = ReportStatus.OPEN.name
                it[urgent] = null
                it[volunteersNeeded] = null
                it[createdAt] = now - 6 * hour
                it[viewCount] = 11
            } get ReportsTable.id

            // Report 6: Nuisance - VANDALISM (In Progress) - Claudius Prinsenlaan
            val report6Id = ReportsTable.insert {
                it[type] = ReportType.NUISANCE.name
                it[ownerId] = claraId
                it[title] = "Graffiti op bushokje Claudius Prinsenlaan"
                it[description] = "Ruiten van het bushokje zijn bekrast en bespoten met zwarte verf."
                it[category] = ReportCategory.VANDALISM.name
                it[latitude] = 51.582
                it[longitude] = 4.795
                it[status] = ReportStatus.IN_PROGRESS.name
                it[urgent] = null
                it[volunteersNeeded] = null
                it[createdAt] = now - 2 * day
                it[viewCount] = 23
            } get ReportsTable.id

            // Report 7: Nuisance - PARKING (Open) - Chasséveld
            val report7Id = ReportsTable.insert {
                it[type] = ReportType.NUISANCE.name
                it[ownerId] = davidId
                it[title] = "Fout geparkeerde bestelbus blokkeert stoep"
                it[description] = "Witte bestelbus staat volledig op het trottoir waardoor voetgangers moeten uitwijken."
                it[category] = ReportCategory.PARKING.name
                it[latitude] = 51.586
                it[longitude] = 4.770
                it[status] = ReportStatus.OPEN.name
                it[urgent] = null
                it[volunteersNeeded] = null
                it[createdAt] = now - 1 * hour
                it[viewCount] = 7
            } get ReportsTable.id

            // Report 8: Help Request - GROCERIES (1 volunteer needed, will be FULL) - Ginnekenweg
            val report8Id = ReportsTable.insert {
                it[type] = ReportType.HELP_REQUEST.name
                it[ownerId] = anastasiaId
                it[title] = "Hulp gezocht bij zware boodschappen"
                it[description] = "Wie kan mij morgenmiddag even helpen sjouwen met boodschappen vanaf de supermarkt?"
                it[category] = ReportCategory.GROCERIES.name
                it[latitude] = 51.582
                it[longitude] = 4.771
                it[status] = ReportStatus.OPEN.name
                it[urgent] = null
                it[volunteersNeeded] = 1
                it[slotStart] = startOfHour + 1 * day
                it[slotEnd] = startOfHour + 1 * day + 2 * hour
                it[createdAt] = now - 4 * hour
                it[viewCount] = 19
            } get ReportsTable.id

            // Report 9: Help Request - MOVING (2 volunteers needed, 1 joined -> OPEN with 1 place) - Zandberg
            val report9Id = ReportsTable.insert {
                it[type] = ReportType.HELP_REQUEST.name
                it[ownerId] = bobId
                it[title] = "Vrijwilligers gezocht voor verhuizen hoekbank"
                it[description] = "Ik zoek twee sterke buurtgenoten om een hoekbank naar de 2e verdieping te tillen."
                it[category] = ReportCategory.MOVING.name
                it[latitude] = 51.580
                it[longitude] = 4.778
                it[status] = ReportStatus.OPEN.name
                it[urgent] = null
                it[volunteersNeeded] = 2
                it[slotStart] = startOfHour + 2 * day
                it[slotEnd] = startOfHour + 2 * day + 3 * hour
                it[createdAt] = now - 1 * day
                it[viewCount] = 34
            } get ReportsTable.id

            // Report 10: Help Request - CHORES (3 volunteers needed, 0 joined -> OPEN with 3 places) - Haagdijk
            val report10Id = ReportsTable.insert {
                it[type] = ReportType.HELP_REQUEST.name
                it[ownerId] = claraId
                it[title] = "Tuin winterklaar maken voor oudere buurtbewoner"
                it[description] = "Gezamenlijk bladeren harken, struiken snoeien en groenafval afvoeren."
                it[category] = ReportCategory.CHORES.name
                it[latitude] = 51.590
                it[longitude] = 4.765
                it[status] = ReportStatus.OPEN.name
                it[urgent] = null
                it[volunteersNeeded] = 3
                it[slotStart] = startOfHour + 3 * day
                it[slotEnd] = startOfHour + 3 * day + 4 * hour
                it[createdAt] = now - 2 * hour
                it[viewCount] = 10
            } get ReportsTable.id

            // Report 11: Help Request - TRANSPORT (1 volunteer needed, Resolved -> CLOSED) - Molengracht
            val report11Id = ReportsTable.insert {
                it[type] = ReportType.HELP_REQUEST.name
                it[ownerId] = davidId
                it[title] = "Ritje naar het ziekenhuis voor controle"
                it[description] = "Vervoer gezocht naar Amphia ziekenhuis Molengracht en na controle weer terug."
                it[category] = ReportCategory.TRANSPORT.name
                it[latitude] = 51.584
                it[longitude] = 4.805
                it[status] = ReportStatus.RESOLVED.name
                it[urgent] = null
                it[volunteersNeeded] = 1
                it[slotStart] = startOfHour - 3 * day
                it[slotEnd] = startOfHour - 3 * day + 2 * hour
                it[createdAt] = now - 4 * day
                it[viewCount] = 52
            } get ReportsTable.id

            // Report 12: Help Request - OTHER (4 volunteers needed, Open) - Breda Zuid
            val report12Id = ReportsTable.insert {
                it[type] = ReportType.HELP_REQUEST.name
                it[ownerId] = evaId
                it[title] = "Buurtpreventie flyeractie opzetten"
                it[description] = "Vrijwilligers gezocht om informatie over inbraakpreventie in de wijk te verspreiden."
                it[category] = ReportCategory.OTHER.name
                it[latitude] = 51.575
                it[longitude] = 4.782
                it[status] = ReportStatus.OPEN.name
                it[urgent] = null
                it[volunteersNeeded] = 4
                it[slotStart] = startOfHour + 5 * day
                it[slotEnd] = startOfHour + 5 * day + 3 * hour
                it[createdAt] = now - 8 * hour
                it[viewCount] = 15
            } get ReportsTable.id

            // Report 13: Distant Report - NUISANCE - NOISE (Tilburg, ~22km away, ownerId = null)
            // Used to prove distance calculation and radius filtering (e.g. radiusKm=5 filters this out)
            // and proves that orphaned/anonymous reports (ownerId == null) function properly.
            val report13Id = ReportsTable.insert {
                it[type] = ReportType.NUISANCE.name
                it[ownerId] = null
                it[title] = "Lawaai van industrieterrein Tilburg"
                it[description] = "Melding buiten Breda geplaatst om radiusfiltering en rapporten zonder eigenaar te testen."
                it[category] = ReportCategory.NOISE.name
                it[latitude] = 51.560
                it[longitude] = 5.080
                it[status] = ReportStatus.OPEN.name
                it[urgent] = null
                it[volunteersNeeded] = null
                it[createdAt] = now - 1 * day
                it[viewCount] = 3
            } get ReportsTable.id

            // -------------------------------------------------------------
            // 3. Votes (Distributed for Ranking & Testing)
            // -------------------------------------------------------------

            // Report 1 has 4 LIKE votes -> Highest score for MOST_LIKED sorting
            VotesTable.insert {
                it[reportId] = report1Id
                it[userId] = bobId
                it[type] = VoteType.LIKE
            }
            VotesTable.insert {
                it[reportId] = report1Id
                it[userId] = claraId
                it[type] = VoteType.LIKE
            }
            VotesTable.insert {
                it[reportId] = report1Id
                it[userId] = davidId
                it[type] = VoteType.LIKE
            }
            VotesTable.insert {
                it[reportId] = report1Id
                it[userId] = evaId
                it[type] = VoteType.LIKE
            }

            // Report 2 has 2 LIKE votes and 1 DISLIKE vote
            // Anastasia has a vote here -> Can test GET /vote/me & DELETE /vote!
            VotesTable.insert {
                it[reportId] = report2Id
                it[userId] = anastasiaId
                it[type] = VoteType.LIKE
            }
            VotesTable.insert {
                it[reportId] = report2Id
                it[userId] = claraId
                it[type] = VoteType.LIKE
            }
            VotesTable.insert {
                it[reportId] = report2Id
                it[userId] = evaId
                it[type] = VoteType.DISLIKE
            }

            // Report 4 has 3 DISLIKE votes (negative score)
            VotesTable.insert {
                it[reportId] = report4Id
                it[userId] = anastasiaId
                it[type] = VoteType.DISLIKE
            }
            VotesTable.insert {
                it[reportId] = report4Id
                it[userId] = claraId
                it[type] = VoteType.DISLIKE
            }
            VotesTable.insert {
                it[reportId] = report4Id
                it[userId] = davidId
                it[type] = VoteType.DISLIKE
            }

            // Report 5 has 2 LIKE votes
            VotesTable.insert {
                it[reportId] = report5Id
                it[userId] = bobId
                it[type] = VoteType.LIKE
            }
            VotesTable.insert {
                it[reportId] = report5Id
                it[userId] = davidId
                it[type] = VoteType.LIKE
            }

            // Report 8 has 1 LIKE vote
            VotesTable.insert {
                it[reportId] = report8Id
                it[userId] = claraId
                it[type] = VoteType.LIKE
            }

            // -------------------------------------------------------------
            // 4. Help Participations (All Availability States Covered)
            // -------------------------------------------------------------

            // Report 8 (capacity: 1, tomorrow): Bob holds the place -> FULL.
            HelpParticipationsTable.insert {
                it[helpRequestId] = report8Id
                it[userId] = bobId
                it[status] = ParticipationStatus.SIGNED_UP.name
                it[joinedAt] = now - 3 * hour
                it[changedAt] = now - 3 * hour
            }

            // Report 8: Clara came later -> first on the waitlist (R6).
            // If Bob leaves, Clara moves up automatically (R8).
            HelpParticipationsTable.insert {
                it[helpRequestId] = report8Id
                it[userId] = claraId
                it[status] = ParticipationStatus.WAITLISTED.name
                it[joinedAt] = now - 2 * hour
                it[changedAt] = now - 2 * hour
            }

            // Report 9 (capacity: 2): Clara signed up -> OPEN with 1 place left.
            // Clara can test DELETE /participants/me!
            HelpParticipationsTable.insert {
                it[helpRequestId] = report9Id
                it[userId] = claraId
                it[status] = ParticipationStatus.SIGNED_UP.name
                it[joinedAt] = now - 20 * hour
                it[changedAt] = now - 20 * hour
            }

            // Report 11 (in the past, RESOLVED): Eva helped and was marked ATTENDED.
            HelpParticipationsTable.insert {
                it[helpRequestId] = report11Id
                it[userId] = evaId
                it[status] = ParticipationStatus.ATTENDED.name
                it[joinedAt] = now - 4 * day
                it[changedAt] = now - 3 * day + 2 * hour
            }

            logger.info("Database seeding successfully completed!")
        }
    }
}

/**
 * Standalone entry point to manually seed or reset the persistent H2 database file.
 */
fun main() {
    val database = Database.connect(
        url = "jdbc:h2:file:./data/brul;DB_CLOSE_ON_EXIT=FALSE",
        driver = "org.h2.Driver",
        user = "sa",
        password = ""
    )

    DatabaseFactory.createSchema(database)

    DatabaseSeeder.seed(database, force = true)
    println("File database at ./data/brul.mv.db has been successfully seeded!")
}
