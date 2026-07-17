package uk.derbyshire.domain.auth

import org.http4k.config.Secret

data class LoginSuccess(
    val sessionToken: Secret
)