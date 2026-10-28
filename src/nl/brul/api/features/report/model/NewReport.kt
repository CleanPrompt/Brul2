// src/nl/brul/api/features/report/model/NewReport.kt
package nl.brul.api.features.report.model

/**
 * Laag: model (het domein).
 *
 * Een melding vóór het opslaan: nog zonder id, want dat geeft de database.
 * Controleert in `init` alles al voordat het de database in gaat:
 * titel en beschrijving niet leeg, de categorie past bij het type, en met `when (type)` de extra
 * velden:
 *
 * - SAFETY: `urgent` verplicht, geen `volunteersNeeded`
 * - NUISANCE: geen van beide
 * - HELP_REQUEST: `volunteersNeeded` minimaal 1, geen `urgent`
 */
data class NewReport(
    val type: ReportType,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val location: Location,
    val urgent: Boolean? = null,
    val volunteersNeeded: Int? = null,
    val ownerId: Long? = null,
    val slot: TimeSlot? = null
) {
    init {
        require(ownerId == null || ownerId > 0) {
            "Owner ID must be positive."
        }

        require(title.isNotBlank()) {
            "Report title cannot be blank."
        }

        require(description.isNotBlank()) {
            "Report description cannot be blank."
        }

        when (type) {
            ReportType.SAFETY -> {
                require(category in type.allowedCategories) {
                    "Invalid safety report category."
                }

                require(urgent != null) {
                    "Safety reports require an urgent value."
                }

                require(volunteersNeeded == null && slot == null) {
                    "Safety reports cannot contain help fields."
                }
            }

            ReportType.NUISANCE -> {
                require(category in type.allowedCategories) {
                    "Invalid nuisance report category."
                }

                require(urgent == null && volunteersNeeded == null && slot == null) {
                    "Nuisance reports cannot contain safety or help fields."
                }
            }

            ReportType.HELP_REQUEST -> {
                require(category in type.allowedCategories) {
                    "Invalid help request category."
                }

                require(volunteersNeeded != null && volunteersNeeded > 0) {
                    "Help requests require at least one volunteer."
                }

                require(urgent == null) {
                    "Help requests cannot contain safety fields."
                }

                // R1: a help request is always planned in a time slot.
                require(slot != null) {
                    "Help requests require a time slot (startsAt and endsAt)."
                }

                require(slot.duration <= HelpRequest.MAX_SLOT_DURATION) {
                    "A help request can last at most ${HelpRequest.MAX_SLOT_DURATION.toHours()} hours."
                }
            }
        }
    }
}
