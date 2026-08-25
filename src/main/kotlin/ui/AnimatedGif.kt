package ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image as SkiaImage
import java.io.File

@Composable
fun AnimatedGif(file: File, modifier: Modifier = Modifier) {
    val codec = remember(file) {
        runCatching { Codec.makeFromData(Data.makeFromBytes(file.readBytes())) }.getOrNull()
    }

    DisposableEffect(file) {
        onDispose { runCatching { codec?.close() } }
    }

    if (codec == null) return

    var frameIndex by remember { mutableStateOf(0) }
    val frameCount = remember(codec) { codec.frameCount.coerceAtLeast(1) }

    LaunchedEffect(codec) {
        while (frameCount > 1) {
            val delayMs = runCatching {
                codec.framesInfo.getOrNull(frameIndex)?.duration?.toLong()?.coerceAtLeast(50L)
            }.getOrNull() ?: 100L
            delay(delayMs)
            frameIndex = (frameIndex + 1) % frameCount
        }
    }

    val imageBitmap: ImageBitmap = remember(codec, frameIndex) {
        val bitmap = Bitmap()
        bitmap.allocPixels(codec.imageInfo)
        codec.readPixels(bitmap, frameIndex)
        SkiaImage.makeFromBitmap(bitmap).toComposeImageBitmap()
    }

    Image(
        bitmap = imageBitmap,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
