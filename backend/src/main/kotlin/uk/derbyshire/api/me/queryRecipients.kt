package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.me.UserConnectionDTO.Companion.toDto
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.services.ConnectionService

fun queryRecipients(connectionService: ConnectionService) = { request: Request ->
    val currentUser = CurrentUser(request)

    val query = QueryRecipientsRequestDTO.lens(request)

    val recipients = connectionService.searchUserRecipients(currentUser.userId, query.deviceMatching?.deviceModelId, query.deviceMatching?.orientation)

    Response(Status.OK).with(UserConnectionDTO.lens of recipients.toDto())
}

private data class QueryRecipientsRequestDTO(
    val deviceMatching: DeviceMatching? = null,
) {
    companion object {
        val lens = Json.autoBody<QueryRecipientsRequestDTO>().toLens()
    }
}

private data class DeviceMatching(
    val deviceModelId: DeviceModelId,
    val orientation: Orientation,
)