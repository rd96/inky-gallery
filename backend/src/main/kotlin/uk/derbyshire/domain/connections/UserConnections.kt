package uk.derbyshire.domain.connections

data class UserConnections(
    val senders: List<UserConnection>,
    val recipients: List<UserConnection>,
)