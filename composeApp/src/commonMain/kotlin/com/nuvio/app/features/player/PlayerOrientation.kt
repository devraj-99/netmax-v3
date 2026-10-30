package com.nuvio.app.features.player

import androidx.compose.runtime.mutableStateOf

/** User-selectable player orientation: Landscape (default, reels off) or
 *  Portrait ("reel format" fullscreen). Read by the platform player effects. */
enum class PlayerOrientationMode {
    Landscape,
    Portrait,
}

object PlayerOrientationPreference {
    var mode = mutableStateOf(PlayerOrientationMode.Landscape)
        private set

    fun toggle() {
        mode.value = when (mode.value) {
            PlayerOrientationMode.Landscape -> PlayerOrientationMode.Portrait
            PlayerOrientationMode.Portrait -> PlayerOrientationMode.Landscape
        }
    }
}
