package com.nuvio.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

// One capture per existing source, shared by every glass surface referencing it.
// Dispose entries with their screen instead of retaining graphics layers forever.
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
