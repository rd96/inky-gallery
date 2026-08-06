package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import uk.derbyshire.domain.drawings.PngDrawing
import uk.derbyshire.domain.drawings.SaveDrawingFailure
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageInputStream

class ImageProcessingService {
    fun canonicaliseToPng(uploadedBytes: ByteArray, expectedWidth: Int, expectedHeight: Int): Result4k<PngDrawing, SaveDrawingFailure> {
        val reader = ImageIO
            .getImageReadersByFormatName("png")
            .asSequence()
            .firstOrNull()
            ?: error("No PNG ImageIO reader is available")

        try {
            MemoryCacheImageInputStream(
                ByteArrayInputStream(uploadedBytes),
            ).use { input ->
                reader.setInput(input, true, true)

                val width = reader.getWidth(0)
                val height = reader.getHeight(0)

                if (width != expectedWidth || height != expectedHeight) {
                    return Failure(SaveDrawingFailure.INVALID_IMAGE_SIZE)
                }

                val uploadedImage = reader.read(0)

                /*
                 * Copy only the pixels into a fresh image. We do not retain
                 * the uploaded PNG's chunks or metadata.
                 */
                val canonicalImage = BufferedImage(
                    width,
                    height,
                    BufferedImage.TYPE_INT_RGB,
                )

                val graphics = canonicalImage.createGraphics()

                try {
                    graphics.drawImage(uploadedImage, 0, 0, null)
                } finally {
                    graphics.dispose()
                }

                return ByteArrayOutputStream().use { output ->
                    val encoded = ImageIO.write(
                        canonicalImage,
                        "png",
                        output,
                    )

                    if (!encoded) Failure(SaveDrawingFailure.UNSUPPORTED_FORMAT)
                    else PngDrawing(
                        widthPx = width,
                        heightPx = height,
                        data = output.toByteArray(),
                    ).asSuccess()
                }
            }
        } catch (e: Exception) {
            return when (e) {
                is IOException,
                is IllegalArgumentException,
                is IndexOutOfBoundsException -> Failure(SaveDrawingFailure.UNSUPPORTED_FORMAT)
                else -> throw e
            }
        } finally {
            reader.dispose()
        }
    }
}