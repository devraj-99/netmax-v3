package com.nuvio.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.kyant.backdrop.Backdrop
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

// A retained RenderNode must never sample an ancestor's retained RenderNode.
// Layout's glass back button did exactly that: capture -> button -> capture.
// A draw-time skip cannot break that graph when Compose reuses a cached layer.
// Consumers therefore sample a flattened bitmap, NEVER the capture GraphicsLayer.
// This preserves pixel refraction while making the GPU dependency graph acyclic.
private val captureClock = TimeSource.Monotonic.markNow()
private var captureDepth = 0
internal fun Modifier.excludeGlassFromCapture(): Modifier = drawWithContent {
    if (captureDepth == 0) drawContent()
}

internal class SnapshotGlassBackdrop(val scope: CoroutineScope) : Backdrop {
    var bitmap: ImageBitmap? by mutableStateOf(null)
    var coordinates: LayoutCoordinates? = null
    var capturing = false
    var lastCaptureMs = -1000L
    override val isCoordinatesDependent = true

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?,
    ) {
        val source = this@SnapshotGlassBackdrop.coordinates ?: return
        val target = coordinates ?: return
        val image = bitmap ?: return
        if (!source.isAttached || !target.isAttached) return
        val offset = runCatching { source.localPositionOf(target) }
            .getOrElse { target.positionInWindow() - source.positionInWindow() }
        withTransform({ translate(-offset.x, -offset.y) }) { drawImage(image) }
    }
}

private val glassBackdrops = mutableMapOf<HazeState, SnapshotGlassBackdrop>()

@Composable
fun rememberGlassHazeState(): HazeState {
    val haze = rememberHazeState()
    val scope = rememberCoroutineScope()
    val captureLayer = rememberGraphicsLayer()
    val backdrop = remember(haze) { SnapshotGlassBackdrop(scope) }
    remember(haze, backdrop, captureLayer) {
        glassBackdrops[haze] = backdrop
        glassCaptureLayers[haze] = captureLayer
    }
    DisposableEffect(haze, backdrop) {
        onDispose {
            if (glassBackdrops[haze] === backdrop) {
                glassBackdrops.remove(haze)
                glassCaptureLayers.remove(haze)
            }
            backdrop.bitmap = null
            backdrop.coordinates = null
        }
    }
    return haze
}

private val glassCaptureLayers = mutableMapOf<HazeState, androidx.compose.ui.graphics.layer.GraphicsLayer>()
internal fun glassBackdropFor(state: HazeState?): Backdrop? = glassBackdrops[state]

fun Modifier.glassSource(state: HazeState): Modifier {
    val backdrop = glassBackdrops[state] ?: return hazeSource(state = state)
    val layer = glassCaptureLayers[state] ?: return hazeSource(state = state)
    return this.onGloballyPositioned { backdrop.coordinates = it }.drawWithContent {
        drawContent()
        val nowMs = captureClock.elapsedNow().inWholeMilliseconds
        // Bound readback cost. A new snapshot is needed only when the source draws.
        if (!backdrop.capturing && nowMs - backdrop.lastCaptureMs >= 50L &&
            size.width >= 1f && size.height >= 1f) {
            backdrop.capturing = true
            backdrop.lastCaptureMs = nowMs
            try {
                layer.record(IntSize(size.width.toInt(), size.height.toInt())) {
                    captureDepth++
                    try { this@drawWithContent.drawContent() } finally { captureDepth-- }
                }
                backdrop.scope.launch {
                    try {
                        backdrop.bitmap = layer.toImageBitmap()
                    } catch (error: Exception) {
                        GlassDiagnostics.lensStatus = "Source snapshot unavailable: ${error::class.simpleName}"
                    } finally { backdrop.capturing = false }
                }
            } catch (error: Exception) {
                backdrop.capturing = false
                GlassDiagnostics.lensStatus = "Source capture unavailable: ${error::class.simpleName}"
            }
        }
    }.hazeSource(state = state)
}

/** Runtime status of the lens shader, shown in the glass settings for troubleshooting. */
object GlassDiagnostics {
    @kotlin.concurrent.Volatile var lensEnabled: Boolean = true
    @kotlin.concurrent.Volatile var lensStatus: String = "Lens shader not drawn yet"
    @kotlin.concurrent.Volatile var path: String = "unknown"
}
