package com.nuvio.app.core.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nuvio.app.features.settings.LiquidGlassSettings

/** Smoked, reflective player material. The video remains visible, but never bare. */
internal object PlayerGlassMaterial {
    fun body(isLight: Boolean): Brush = Brush.verticalGradient(
        0f to Color(0xFF67717D).copy(alpha = if (isLight) 0.46f else 0.38f),
        0.24f to Color(0xFF242B36).copy(alpha = 0.46f),
        0.65f to Color(0xFF131923).copy(alpha = 0.54f),
        1f to Color(0xFF424754).copy(alpha = 0.46f),
    )

    fun reflection(settings: LiquidGlassSettings): Brush {
        val reflection = settings.reflectionIntensity.coerceIn(0f, 2f)
        val lens = settings.refractionAmount.coerceIn(0f, 1f)
        fun shine(alpha: Float) = Color.White.copy(alpha = (alpha * reflection).coerceIn(0f, 1f))
        return Brush.verticalGradient(
            0f to shine(0.26f),
            0.06f to shine(0.12f),
            0.22f to shine(0.025f),
            0.55f to Color.Transparent,
            0.83f to Color.Transparent,
            1f to shine(0.10f * lens),
        )
    }

    fun sheen(settings: LiquidGlassSettings): Brush {
        val reflection = settings.reflectionIntensity.coerceIn(0f, 2f)
        fun shine(alpha: Float) = Color.White.copy(alpha = (alpha * reflection).coerceIn(0f, 1f))
        return Brush.linearGradient(
            0f to shine(0.13f),
            0.15f to shine(0.035f),
            0.36f to Color.Transparent,
            0.72f to Color.Transparent,
            0.91f to shine(0.025f),
            1f to shine(0.11f),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
    }

    fun rim(settings: LiquidGlassSettings): Brush {
        val reflection = settings.reflectionIntensity.coerceIn(0f, 2f)
        fun shine(alpha: Float) = Color.White.copy(alpha = (alpha * reflection).coerceIn(0f, 1f))
        return Brush.linearGradient(
            0f to shine(0.82f),
            0.18f to shine(0.42f),
            0.43f to shine(0.10f),
            0.70f to shine(0.06f),
            1f to shine(0.50f),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
    }
}
