// src/nl/brul/api/common/error/DomainExceptions.kt
package nl.brul.api.common.error

/**
 * Laag: common (gedeeld door alle features).
 *
 * Exceptions die een service gooit als een bedrijfsregel wordt gebroken.
 * Elk type hoort bij één HTTP-status in ErrorHandling.kt. De [code] komt in de JSON-foutmelding,
 * bijvoorbeeld `{"message": "...", "code": "TIME_CONFLICT"}`, zodat de app precies weet welke
 * regel er misging en de gebruiker een passende melding kan tonen.
 * `open class`: een feature maakt er zijn eigen, specifiekere fouten van (zie
 * HelpParticipationExceptions). Een gewone Kotlin-klasse is `final`; zonder `open` kun je er
 * niet van erven.
 */

/** 404: wat je zoekt, bestaat niet. */
open class DomainNotFoundException(
    val code: String,
    message: String
) : RuntimeException(message)

/** 403: je bent ingelogd, maar je mag dit niet. */
open class DomainForbiddenException(
    val code: String,
    message: String
) : RuntimeException(message)

/** 409: het verzoek klopt, maar botst met de huidige toestand, bijvoorbeeld een volle agenda. */
open class DomainConflictException(
    val code: String,
    message: String
) : RuntimeException(message)

/** 413: de upload is groter dan toegestaan. */
class PayloadTooLargeException(
    val code: String,
    message: String
) : RuntimeException(message)

/** 415: de upload is een soort bestand dat we niet accepteren. */
class UnsupportedMediaTypeException(
    val code: String,
    message: String
) : RuntimeException(message)
