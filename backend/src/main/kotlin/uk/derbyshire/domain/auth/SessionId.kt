package uk.derbyshire.domain.auth

import kotlin.uuid.Uuid

@JvmInline
value class SessionId(val value: Uuid)