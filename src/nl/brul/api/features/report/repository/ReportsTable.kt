// src/nl/brul/api/features/report/repository/ReportsTable.kt
package nl.brul.api.features.report.repository

import org.jetbrains.exposed.sql.Table

object ReportsTable : Table("reports") {

    val id = long("id").autoIncrement()

    val type = varchar("type", 30)

    val title = varchar("title", 120)

    val description = text("description")

    val latitude = double("latitude")

    val longitude = double("longitude")

    val status = varchar("status", 30)

    val urgent = bool("urgent").nullable()

    val category = varchar("category", 100)

    val volunteersNeeded = integer("volunteers_needed").nullable()

    override val primaryKey = PrimaryKey(id)
}