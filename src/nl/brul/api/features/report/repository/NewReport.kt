// src/nl/brul/api/features/report/repository/NewReport.kt
package nl.brul.api.features.report.repository

import nl.brul.api.features.report.model.Location
import nl.brul.api.features.report.model.ReportCategory
import nl.brul.api.features.report.model.ReportType

data class NewReport(
    val type: ReportType,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val location: Location,
    val urgent: Boolean? = null,
    val volunteersNeeded: Int? = null
) {
    init {
        require(title.isNotBlank()) {
            "Report title cannot be blank."
        }

        require(description.isNotBlank()) {
            "Report description cannot be blank."
        }

        when (type) {
            ReportType.SAFETY -> {
                require(
                    category in setOf(
                        ReportCategory.THEFT,
                        ReportCategory.SUSPICIOUS_SITUATION,
                        ReportCategory.TRAFFIC_SAFETY
                    )
                ) {
                    "Invalid safety report category."
                }

                require(urgent != null) {
                    "Safety reports require an urgent value."
                }

                require(volunteersNeeded == null) {
                    "Safety reports cannot contain help fields."
                }
            }

            ReportType.NUISANCE -> {
                require(
                    category in setOf(
                        ReportCategory.NOISE,
                        ReportCategory.WASTE,
                        ReportCategory.VANDALISM,
                        ReportCategory.PARKING
                    )
                ) {
                    "Invalid nuisance report category."
                }

                require(urgent == null && volunteersNeeded == null) {
                    "Nuisance reports cannot contain safety or help fields."
                }
            }

            ReportType.HELP_REQUEST -> {
                require(
                    category in setOf(
                        ReportCategory.GROCERIES,
                        ReportCategory.TRANSPORT,
                        ReportCategory.MOVING,
                        ReportCategory.CHORES,
                        ReportCategory.OTHER
                    )
                ) {
                    "Invalid help request category."
                }

                require(volunteersNeeded != null && volunteersNeeded > 0) {
                    "Help requests require at least one volunteer."
                }

                require(urgent == null) {
                    "Help requests cannot contain safety fields."
                }
            }
        }
    }
}