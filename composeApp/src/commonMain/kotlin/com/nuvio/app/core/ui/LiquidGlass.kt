package com.nuvio.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.features.settings.LiquidGlassSettings
import com.nuvio.app.features.settings.LiquidGlassSettingsRepository
import com.nuvio.app.features.settings.ThemeSettingsRepository
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * App-wide ambient haze state. Provided at the tab-host root so any [Modifier.liquidGlass]
 * call without an explicit hazeState still blurs the real content behind it instead of
 * rendering as plain transparency.
 */
val LocalAppHazeState = compositionLocalOf<HazeState?> { null }

object LiquidGlassDefaults {
    val BlurRadius: Dp = 32.dp
    val PillShape: Shape = RoundedCornerShape(NuvioTokens.Radius.full)
    val CardShape: Shape = RoundedCornerShape(18.dp)
    val ButtonShape: Shape = CircleShape

    @Composable
    fun glassBrush(isLight: Boolean, settings: LiquidGlassSettings, dynamicTint: Color? = null): Brush {
        val vibrancy = (0.85f + settings.vibrancy * 0.15f).coerceIn(0.7f, 1.35f)
        if (dynamicTint != null) {
            val tintAlpha = if (isLight) 0.14f * vibrancy else 0.18f * vibrancy
            return if (isLight) {
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.52f * vibrancy),
                        dynamicTint.copy(alpha = tintAlpha),
                        Color(0xFFEBEBF5).copy(alpha = 0.35f * vibrancy),
                    ),
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF323242).copy(alpha = 0.30f * vibrancy),
                        dynamicTint.copy(alpha = tintAlpha),
                        Color(0xFF101016).copy(alpha = 0.42f * vibrancy),
                    ),
                )
            }
        }
        if (settings.enhancedLiquidGlass) {
            return if (isLight) {
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f * vibrancy),
                        Color(0xFFF6F6FC).copy(alpha = 0.30f * vibrancy),
                        Color(0xFFE5E5EE).copy(alpha = 0.38f * vibrancy),
                    ),
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF383848).copy(alpha = 0.30f * vibrancy),
                        Color(0xFF1E1E28).copy(alpha = 0.24f * vibrancy),
                        Color(0xFF101016).copy(alpha = 0.40f * vibrancy),
                    ),
                )
            }
        }
        return if (isLight) {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.42f * vibrancy),
                    Color(0xFFF2F2F7).copy(alpha = 0.30f * vibrancy),
                ),
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF303038).copy(alpha = 0.30f * vibrancy),
                    Color(0xFF111116).copy(alpha = 0.40f * vibrancy),
                ),
            )
        }
    }

    @Composable
    fun borderBrush(isLight: Boolean, settings: LiquidGlassSettings): Brush {
        val chroma = settings.chromaticAberration
        val reflection = settings.reflectionIntensity.coerceIn(0f, 2f)
        val base = if (settings.enhancedLiquidGlass) {
            if (isLight) {
                listOf(
                    Color.White.copy(alpha = 0.90f),
                    Color.White.copy(alpha = 0.35f),
                    Color(0xFFD8D8E5).copy(alpha = 0.20f),
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.60f),
                    Color.White.copy(alpha = 0.18f),
                    Color.White.copy(alpha = 0.06f),
                )
            }
        } else {
            if (isLight) {
                listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.40f),
                    Color(0xFFD0D0D8).copy(alpha = 0.25f),
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.65f),
                    Color.White.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.06f),
                )
            }
        }
        val reflected = base.map { it.copy(alpha = (it.alpha * reflection).coerceIn(0f, 1f)) }
        if (chroma <= 0.01f) return Brush.verticalGradient(reflected)
        val edge = (0.22f * chroma * reflection).coerceIn(0f, 0.22f)
        return Brush.horizontalGradient(
            listOf(
                Color(0xFF68D8D6).copy(alpha = edge),
                reflected[0],
                reflected[1],
                reflected[2],
                Color(0xFFFF69B4).copy(alpha = edge),
            ),
        )
    }

    fun refractionBrush(settings: LiquidGlassSettings, isLight: Boolean): Brush {
        val amount = settings.refractionAmount.coerceIn(0f, 1f)
        val height = settings.refractionHeight.coerceIn(0.05f, 1f)
        if (settings.enhancedLiquidGlass) {
            val tint = if (isLight) Color.White else Color(0xFFF0F6FF)
            return Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to tint.copy(alpha = 0.42f * amount),
                    (height * 0.35f).coerceAtMost(0.35f) to tint.copy(alpha = 0.14f * amount),
                    height to tint.copy(alpha = 0.03f * amount),
                    (height + 0.20f).coerceAtMost(0.85f) to Color.Transparent,
                    0.85f to Color.Transparent,
                    1f to tint.copy(alpha = 0.08f * amount), // ambient bottom bounce
                ),
            )
        }
        val tint = if (isLight) Color.White else Color(0xFFE9F5FF)
        return Brush.verticalGradient(
            colorStops = arrayOf(
                0f to tint.copy(alpha = 0.40f * amount),
                (height * 0.4f).coerceAtMost(0.4f) to tint.copy(alpha = 0.16f * amount),
                height to tint.copy(alpha = 0.05f * amount),
                (height + 0.18f).coerceAtMost(0.9f) to Color.Transparent,
                0.9f to Color.Transparent,
                1f to tint.copy(alpha = 0.07f * amount),
            ),
        )
    }
}

enum class GlassComponent { General, Player, MiniPlayer, Navigation }

@Composable
fun Modifier.liquidGlass(
    shape: Shape = LiquidGlassDefaults.PillShape,
    hazeState: HazeState? = null,
    isEnabled: Boolean = true,
    borderWidth: Dp = 1.dp,
    component: GlassComponent = GlassComponent.General,
    alphaFactor: Float = 1f,
    dynamicTint: Color? = null,
): Modifier {
    LiquidGlassSettingsRepository.ensureLoaded()
    val settings by LiquidGlassSettingsRepository.uiState.collectAsStateWithLifecycle()
    val componentEnabled = when (component) {
        GlassComponent.General -> true
        GlassComponent.Player -> settings.playerEnabled > 0.5f
        GlassComponent.MiniPlayer -> settings.miniPlayerEnabled > 0.5f
        GlassComponent.Navigation -> settings.navigationEnabled > 0.5f
    }
    val effectiveEnabled = isEnabled && settings.enabled && componentEnabled
    val resolvedHazeState = hazeState ?: LocalAppHazeState.current
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f

    if (!effectiveEnabled) {
        GlassDiagnostics.path = "glass off (plain surface)"
        return this
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
            .border(borderWidth, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), shape)
    }

    val backdrop = glassBackdropFor(resolvedHazeState)
    if (backdrop != null && shape is androidx.compose.foundation.shape.CornerBasedShape &&
        com.kyant.backdrop.isRuntimeShaderSupported()) {
        GlassDiagnostics.path = "backdrop lens"
        return this.excludeGlassFromCapture()
            .refractiveGlass(backdrop, shape, settings, borderWidth, alphaFactor)
    }

    GlassDiagnostics.path = when {
        backdrop == null -> "FALLBACK blur: no backdrop source for this surface"
        shape !is androidx.compose.foundation.shape.CornerBasedShape -> "FALLBACK blur: shape not rounded"
        else -> "FALLBACK blur: Android < 13, no runtime shader"
    }
    // Older Android fallback: honest blur/highlights, without claiming refraction.
    // Zero blur remains clear here too, rather than leaving a milky tint behind.
    val frosting = (settings.blurRadius / 32f).coerceIn(0f, 1f)
    val glassBorder = LiquidGlassDefaults.borderBrush(isLight, settings)
    val tintAlpha = (settings.surfaceOpacity * alphaFactor * frosting * 0.35f).coerceIn(0f, 1f)
    val depth = (settings.depthEffect * (if (settings.enhancedLiquidGlass) 18f else 14f)).dp
    val blurRadius = if (settings.enhancedLiquidGlass) {
        (settings.blurRadius * 1.35f).coerceAtLeast(32f).dp
    } else {
        settings.blurRadius.dp
    }
    val noise = if (settings.enhancedLiquidGlass) {
        0.010f
    } else {
        (0.01f + settings.vibrancy * 0.015f).coerceIn(0.01f, 0.04f)
    }

    return this
        .shadow(depth, shape, clip = false)
        .clip(shape)
        .then(
            if (resolvedHazeState != null && settings.blurRadius > 0f) {
                Modifier.hazeEffect(state = resolvedHazeState) {
                    this.blurRadius = blurRadius
                    inputScale = HazeInputScale.Fixed(0.5f)
                    noiseFactor = noise
                    if (settings.blurTint.alpha > 0f) {
                        tints = listOf(HazeTint(settings.blurTint))
                    }
                }
            } else Modifier
        )
        .background(settings.surfaceTint.copy(alpha = tintAlpha), shape)
        .then(
            if (dynamicTint != null && frosting > 0f) {
                Modifier.background(dynamicTint.copy(alpha = (0.12f * alphaFactor * frosting).coerceIn(0f, 0.35f)), shape)
            } else Modifier
        )
        .border(borderWidth, glassBorder, shape)
        .border(
            (borderWidth * settings.rimWidth).coerceAtMost(4.dp),
            Color.White.copy(alpha = (settings.rimIntensity * 0.24f).coerceIn(0f, 0.8f)),
            shape,
        )
}

@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    isEnabled: Boolean = true,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    tint: Color? = null,
    dynamicTint: Color? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    LiquidGlassSettingsRepository.ensureLoaded()
    val settings by LiquidGlassSettingsRepository.uiState.collectAsStateWithLifecycle()
    val resolvedTint = tint ?: settings.textColor

    BoxWithLiquidGlassButton(
        onClick = onClick,
        icon = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        hazeState = hazeState,
        isEnabled = isEnabled,
        size = size,
        iconSize = iconSize,
        tint = resolvedTint,
        dynamicTint = dynamicTint,
        interactionSource = interactionSource,
    )
}

@Composable
private fun BoxWithLiquidGlassButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier,
    hazeState: HazeState?,
    isEnabled: Boolean,
    size: Dp,
    iconSize: Dp,
    tint: Color,
    dynamicTint: Color?,
    interactionSource: MutableInteractionSource,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(size)
            .liquidGlass(
                shape = LiquidGlassDefaults.ButtonShape,
                hazeState = hazeState,
                isEnabled = isEnabled,
                dynamicTint = dynamicTint,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = size / 2),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun LiquidGlassBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    isEnabled: Boolean = true,
    contentDescription: String? = "Back",
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    dynamicTint: Color? = null,
) {
    LiquidGlassIconButton(
        onClick = onBack,
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = contentDescription,
        modifier = modifier,
        hazeState = hazeState,
        isEnabled = isEnabled,
        size = size,
        iconSize = iconSize,
        dynamicTint = dynamicTint,
    )
}

@Composable
fun LiquidGlassTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    isEnabled: Boolean = true,
    dynamicTint: Color? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    LiquidGlassSettingsRepository.ensureLoaded()
    val settings by LiquidGlassSettingsRepository.uiState.collectAsStateWithLifecycle()
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .liquidGlass(
                shape = LiquidGlassDefaults.PillShape,
                hazeState = hazeState,
                isEnabled = isEnabled,
                dynamicTint = dynamicTint,
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                if (onBack != null) {
                    LiquidGlassBackButton(
                        onBack = onBack,
                        hazeState = hazeState,
                        isEnabled = isEnabled,
                        size = 36.dp,
                        iconSize = 18.dp,
                        dynamicTint = dynamicTint,
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = settings.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) { actions() }
        }
    }
}
