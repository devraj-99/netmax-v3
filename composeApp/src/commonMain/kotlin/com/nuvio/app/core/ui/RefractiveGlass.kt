package com.nuvio.app.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CornerBasedShape
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.nuvio.app.features.settings.LiquidGlassSettings

/** Real source-pixel refraction. No opaque body or decorative gradient wash. */
internal fun Modifier.refractiveGlass(
    backdrop: Backdrop,
    shape: Shape,
    settings: LiquidGlassSettings,
    borderWidth: androidx.compose.ui.unit.Dp,
    alphaFactor: Float,
): Modifier = drawBackdrop(
    backdrop = backdrop,
    shape = { shape },
    effects = {
        // Blur is deliberately small: the rounded lens, not heavy frosting,
        // provides the liquid shape. Zero means no blur and no colour wash.
        if (settings.blurRadius > 0f) blur((settings.blurRadius * 0.25f).dp.toPx())
        val cornerShape = shape as? CornerBasedShape
        if (cornerShape != null) {
            val corner = minOf(
                cornerShape.topStart.toPx(size, this),
                cornerShape.topEnd.toPx(size, this),
                cornerShape.bottomStart.toPx(size, this),
                cornerShape.bottomEnd.toPx(size, this),
                size.minDimension / 2f,
            )
            lens(
                refractionHeight = (size.minDimension * 0.24f * settings.refractionHeight)
                    .coerceAtMost(corner).coerceAtLeast(0f),
                refractionAmount = (size.minDimension * 0.28f * settings.refractionAmount)
                    .coerceIn(0f, size.minDimension),
                depthEffect = settings.depthEffect > 0.01f,
                chromaticAberration = settings.chromaticAberration > 0.01f,
            )
        }
    },
    highlight = {
        if (borderWidth.value <= 0f || settings.reflectionIntensity <= 0f) null
        else Highlight(
            width = borderWidth.coerceAtMost(1.dp),
            alpha = (settings.reflectionIntensity * 0.7f).coerceIn(0f, 1f),
            style = HighlightStyle.Default,
        )
    },
    // Avoid a costly large shadow per icon, especially during video playback.
    shadow = null,
    onDrawSurface = {
        if (settings.blurRadius > 0f) {
            val frost = (settings.blurRadius / 32f).coerceIn(0f, 1f)
            drawRect(settings.surfaceTint.copy(alpha =
                (settings.surfaceOpacity * frost * alphaFactor * 0.35f).coerceIn(0f, 0.25f)))
            if (settings.blurTint.alpha > 0f) {
                drawRect(settings.blurTint.copy(alpha = settings.blurTint.alpha * frost))
            }
        }
    },
)
