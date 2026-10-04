package nl.brul.domain

class NuisanceReport(
    title: String,
    description: String?,
    photo: String?,
    location: Location,
    createdBy: User,
    val nuisanceType: NuisanceType
) : Report(
    title = title,
    description = description,
    photo = photo,
    location = location,
    createdBy = createdBy
)