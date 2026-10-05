package nl.brul.service

class ReportNotFoundException(
    reportId: String
) : RuntimeException(
    "Report with id '$reportId' was not found."
)