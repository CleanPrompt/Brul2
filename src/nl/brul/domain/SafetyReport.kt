package nl.brul.domain

class SafetyReport(
    title: String,
    description: String?,
    photo: String?,
    location: Location,
    createdBy: User,
    val urgency: Urgency
) : Report(
    title = title,
    description = description,
    photo = photo,
    location = location,
    createdBy = createdBy
)