package com.nuvio.app.features.player

import androidx.compose.animation.core.Easing
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

// Zero slope at both ends avoids a visible shoulder as the blur meets clear video.
internal fun playerEdgeBlurStrength(distanceFromEdge: Float): Float {
    val t = (distanceFromEdge / 0.92f).coerceIn(0f, 1f)
    return 1f - t * t * (3f - 2f * t)
}

internal val PlayerEdgeBlurEasing = Easing { fraction ->
    val t = fraction.coerceIn(0f, 1f)
    t * t * (3f - 2f * t)
}

/**
 * Fade the whole rendered effect, not just its blur radius. A progressive blur can
 * still contain an opaque copy of the source, tint or noise at radius zero.
 * Keep this outside hazeEffect so DstIn masks its final output on its own layer,
 * never the underlying video or the controls. The last 8% is fully transparent.
 */
internal fun Modifier.playerEdgeBlurFade(top: Boolean): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val stops = Array(33) { index ->
                val position = index / 32f
                val distance = if (top) position else 1f - position
                position to Color.White.copy(alpha = playerEdgeBlurStrength(distance))
            }
            val fade = Brush.verticalGradient(colorStops = stops)
            onDrawWithContent {
                drawContent()
                drawRect(brush = fade, blendMode = BlendMode.DstIn)
            }
        }
