// src/nl/brul/api/features/auth/service/EmailAlreadyRegisteredException.kt
package nl.brul.api.features.auth.service

class EmailAlreadyRegisteredException :
    RuntimeException("Email address is already registered.")