package com.nuvio.app.features.equalizer

import kotlin.math.log10
import kotlin.math.roundToInt

/** A linear amplitude target, not a promise of perceived loudness. */
internal object VolumeBoost {
    fun clamp(percent: Int): Int = percent.coerceIn(100, 200)

    fun gainDb(percent: Int): Double = 20.0 * log10(clamp(percent) / 100.0)

    fun gainMillibels(percent: Int): Int = (gainDb(percent) * 100.0).roundToInt()
}
