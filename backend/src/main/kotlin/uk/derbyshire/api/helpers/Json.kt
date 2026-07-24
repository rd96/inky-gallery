package uk.derbyshire.api.helpers

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.format.ConfigurableJackson
import org.http4k.format.asConfigurable
import org.http4k.format.text
import org.http4k.format.withStandardMappings
import uk.derbyshire.domain.devices.HexColour
import kotlin.time.Instant
import kotlin.uuid.Uuid

object Json : ConfigurableJackson(
    KotlinModule.Builder()
        .build()
        .asConfigurable()
        .withStandardMappings()
        .text(
            { value -> Uuid.parse(value) },
            { uuid -> uuid.toString() },
        )
        .text(
            { value -> Instant.parse(value) },
            { instant -> instant.toString() },
        )
        .text(
            { value -> HexColour.parse(value) },
            { hexColour -> hexColour.toString() },
        )
        .done(),
)