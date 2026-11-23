package com.nuvio.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

// One capture per existing source, shared by every glass surface referencing it.
// Dispose entries with their screen instead of retaining graphics layers forever.
// Sources and consumers must be sibling subtrees. A draw-time exclusion
// cannot remove cached RenderNodes and is not a safety boundary.
internal fun Modifier.excludeGlassFromCapture(): Modifier = this

private val glassBackdrops = mutableMapOf<HazeState, LayerBackdrop>()

@Composable
fun rememberGlassHazeState(): HazeState {
    val haze = rememberHazeState()
    val backdrop = rememberLayerBackdrop()
    remember(haze, backdrop) { glassBackdrops[haze] = backdrop }
    DisposableEffect(haze, backdrop) {
        onDispose { if (glassBackdrops[haze] === backdrop) glassBackdrops.remove(haze) }
    }
    return haze
}

internal fun glassBackdropFor(state: HazeState?): LayerBackdrop? = glassBackdrops[state]

fun Modifier.glassSource(state: HazeState): Modifier {
    val backdrop = glassBackdrops[state]
    return (if (backdrop != null) this.layerBackdrop(backdrop) else this)
        .hazeSource(state = state)
}

/** Runtime status of the lens shader, shown in the glass settings for troubleshooting. */
object GlassDiagnostics {
    @kotlin.concurrent.Volatile var lensEnabled: Boolean = true
    @kotlin.concurrent.Volatile var lensStatus: String = "Lens shader not drawn yet"
    @kotlin.concurrent.Volatile var path: String = "unknown"
}
