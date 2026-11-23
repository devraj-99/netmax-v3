package com.nuvio.app.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CornerBasedShape
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.effects.colorControls
import androidx.compose.foundation.border
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
        if (settings.blurRadius > 0f) {
            colorControls(saturation = settings.vibrancy)
            blur((settings.blurRadius * (if (settings.enhancedLiquidGlass) 0.35f else 0.25f)).dp.toPx())
        }
        val cornerShape = shape as? CornerBasedShape
        if (cornerShape != null) {
            val corner = minOf(
                cornerShape.topStart.toPx(size, this),
                cornerShape.topEnd.toPx(size, this),
                cornerShape.bottomStart.toPx(size, this),
                cornerShape.bottomEnd.toPx(size, this),
                size.minDimension / 2f,
            )
            // Echo-like: the bent band covers most of the rounded end caps and the
            // displacement is large enough to see content bend at the edge.
            val height = (corner * 1.1f * settings.refractionHeight)
                .coerceIn(0f, corner.coerceAtLeast(0f))
            val amount = (size.minDimension * 0.65f * settings.refractionAmount)
                .coerceIn(0f, size.minDimension)
            if (height > 0f && amount > 0f && GlassDiagnostics.lensEnabled) {
                try {
                    val width = size.width
                    val surfaceHeight = size.height
                    val offset = -(padding - height).coerceAtLeast(0f)
                    padding = (padding - height).coerceAtLeast(0f)
                    runtimeShaderEffect("NetmaxLens", GlassLensShader, "content") {
                        setFloatUniform("size", width, surfaceHeight)
                        setFloatUniform("offset", offset, offset)
                        setFloatUniform("cornerRadii", floatArrayOf(corner, corner, corner, corner))
                        setFloatUniform("refractionHeight", height)
                        setFloatUniform("refractionAmount", -amount)
                        setFloatUniform("depthEffect", settings.depthEffect)
                        setFloatUniform("chromaticAberration", settings.chromaticAberration)
                    }
                    GlassDiagnostics.lensStatus = "Lens shader active (refraction ${height.toInt()}px / ${amount.toInt()}px)"
                } catch (t: Throwable) {
                    // Never crash the app for a visual effect; fall back to blur-only.
                    GlassDiagnostics.lensEnabled = false
                    GlassDiagnostics.lensStatus = "Lens shader FAILED, blur only: ${t::class.simpleName}: ${t.message?.take(80)}"
                }
            }
        }
    },
    highlight = {
        if (borderWidth.value <= 0f || settings.reflectionIntensity <= 0f) null
        else Highlight(
            width = borderWidth.coerceAtMost(1.dp),
            alpha = (settings.reflectionIntensity * 0.7f).coerceIn(0f, 1f),
            style = HighlightStyle.Default(angle = settings.reflectionAngle),
        )
    },
    // Avoid a costly large shadow per icon, especially during video playback.
    shadow = null,
    onDrawSurface = {
        if (settings.blurRadius > 0f) {
            val frost = (settings.blurRadius / 32f).coerceIn(0f, 1f)
            drawRect(settings.surfaceTint.copy(alpha =
                (settings.surfaceOpacity * (0.35f + 0.65f * frost) * alphaFactor * 1.4f).coerceIn(0f, 0.45f)))
            if (settings.blurTint.alpha > 0f) {
                drawRect(settings.blurTint.copy(alpha = settings.blurTint.alpha * frost))
            }
        }
    },
).then(
    if (borderWidth.value > 0f && settings.rimWidth > 0f && settings.rimIntensity > 0f) {
        Modifier.border(
            (borderWidth * settings.rimWidth).coerceAtMost(4.dp),
            Color.White.copy(alpha = (0.24f * settings.rimIntensity).coerceIn(0f, 0.8f)),
            shape,
        )
    } else Modifier
)
