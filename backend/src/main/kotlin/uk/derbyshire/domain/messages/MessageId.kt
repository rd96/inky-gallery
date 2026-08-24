package uk.derbyshire.domain.messages

import kotlin.uuid.Uuid

@JvmInline
value class MessageId(val value: Uuid)