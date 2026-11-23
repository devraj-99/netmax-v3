package com.nuvio.app.features.settings

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.nuvio.app.core.sync.decodeSyncBoolean
import com.nuvio.app.core.sync.decodeSyncFloat
import com.nuvio.app.core.sync.decodeSyncString
import com.nuvio.app.core.sync.encodeSyncBoolean
import com.nuvio.app.core.sync.encodeSyncFloat
import com.nuvio.app.core.sync.encodeSyncString

@Stable
data class LiquidGlassSettings(
    val enabled: Boolean = true,
    val enhancedLiquidGlass: Boolean = false,
    val vibrancy: Float = 1f,
    val blurRadius: Float = 32f,
    val refractionHeight: Float = 0.7f,
    val refractionAmount: Float = 0.6f,
    val chromaticAberration: Float = 0.4f,
    val depthEffect: Float = 0.55f,
    val surfaceTint: Color = Color.White,
    val surfaceOpacity: Float = 0.18f,
    val textColor: Color = Color.White,
    val rimIntensity: Float = 1f,
    val rimWidth: Float = 1f,
    val reflectionAngle: Float = 45f,
    val playerEnabled: Float = 1f,
    val miniPlayerEnabled: Float = 1f,
    val navigationEnabled: Float = 1f,
    val reflectionIntensity: Float = 1f,
    val blurTint: Color = Color.Transparent,
)

object LiquidGlassSettingsRepository {
    private val _uiState = MutableStateFlow(LiquidGlassSettings())
    val uiState: StateFlow<LiquidGlassSettings> = _uiState.asStateFlow()
    private var loaded = false

    fun ensureLoaded() {
        if (loaded) return
        loaded = true
        _uiState.value = LiquidGlassSettings(
            enabled = ThemeSettingsStorage.loadLiquidGlassEnabled() ?: true,
            enhancedLiquidGlass = ThemeSettingsStorage.loadEnhancedLiquidGlass() ?: false,
            vibrancy = (ThemeSettingsStorage.loadLiquidGlassVibrancy() ?: 1f).coerceIn(0f, 2f),
            blurRadius = (ThemeSettingsStorage.loadLiquidGlassBlurRadius() ?: 32f).coerceIn(0f, 48f),
            refractionHeight = (ThemeSettingsStorage.loadLiquidGlassRefractionHeight() ?: 0.7f).coerceIn(0f, 1f),
            refractionAmount = (ThemeSettingsStorage.loadLiquidGlassRefractionAmount() ?: 0.6f).coerceIn(0f, 1f),
            chromaticAberration = (ThemeSettingsStorage.loadLiquidGlassChromaticAberration() ?: 0.4f).coerceIn(0f, 1f),
            depthEffect = (ThemeSettingsStorage.loadLiquidGlassDepthEffect() ?: 0.55f).coerceIn(0f, 1f),
            surfaceTint = decodeColor(ThemeSettingsStorage.loadLiquidGlassSurfaceTint()) ?: Color.White,
            surfaceOpacity = (ThemeSettingsStorage.loadLiquidGlassSurfaceOpacity() ?: 0.18f).coerceIn(0f, 0.5f),
            textColor = decodeColor(ThemeSettingsStorage.loadLiquidGlassTextColor()) ?: Color.White,
            rimIntensity = (ThemeSettingsStorage.loadLiquidGlassRimIntensity() ?: 1f).coerceIn(0f, 2f),
            rimWidth = (ThemeSettingsStorage.loadLiquidGlassRimWidth() ?: 1f).coerceIn(0f, 3f),
            reflectionAngle = (ThemeSettingsStorage.loadLiquidGlassReflectionAngle() ?: 45f).coerceIn(0f, 360f),
            playerEnabled = (ThemeSettingsStorage.loadLiquidGlassPlayerEnabled() ?: 1f).coerceIn(0f, 1f),
            miniPlayerEnabled = (ThemeSettingsStorage.loadLiquidGlassMiniPlayerEnabled() ?: 1f).coerceIn(0f, 1f),
            navigationEnabled = (ThemeSettingsStorage.loadLiquidGlassNavigationEnabled() ?: 1f).coerceIn(0f, 1f),
            reflectionIntensity = (ThemeSettingsStorage.loadLiquidGlassReflectionIntensity() ?: 1f).coerceIn(0f, 2f),
            blurTint = decodeColor(ThemeSettingsStorage.loadLiquidGlassBlurTint()) ?: Color.Transparent,
        )
    }

    fun onProfileChanged() {
        loaded = false
        ensureLoaded()
    }

    fun clearLocalState() {
        loaded = false
        _uiState.value = LiquidGlassSettings()
    }

    fun setEnabled(value: Boolean) = update { it.copy(enabled = value) }
    fun setEnhancedLiquidGlass(value: Boolean) = update { it.copy(enhancedLiquidGlass = value) }
    fun setVibrancy(value: Float) = update { it.copy(vibrancy = value.coerceIn(0f, 2f)) }
    fun setBlurRadius(value: Float) = update { it.copy(blurRadius = value.coerceIn(0f, 48f)) }
    fun setRefractionHeight(value: Float) = update { it.copy(refractionHeight = value.coerceIn(0f, 1f)) }
    fun setRefractionAmount(value: Float) = update { it.copy(refractionAmount = value.coerceIn(0f, 1f)) }
    fun setChromaticAberration(value: Float) = update { it.copy(chromaticAberration = value.coerceIn(0f, 1f)) }
    fun setDepthEffect(value: Float) = update { it.copy(depthEffect = value.coerceIn(0f, 1f)) }
    fun setSurfaceTint(value: Color) = update { it.copy(surfaceTint = value) }
    fun setSurfaceOpacity(value: Float) = update { it.copy(surfaceOpacity = value.coerceIn(0f, 0.5f)) }
    fun setTextColor(value: Color) = update { it.copy(textColor = value) }
    fun setRimIntensity(value: Float) = update { it.copy(rimIntensity = value.coerceIn(0f, 2f)) }
    fun setRimWidth(value: Float) = update { it.copy(rimWidth = value.coerceIn(0f, 3f)) }
    fun setReflectionAngle(value: Float) = update { it.copy(reflectionAngle = value.coerceIn(0f, 360f)) }
    fun setPlayerEnabled(value: Float) = update { it.copy(playerEnabled = value.coerceIn(0f, 1f)) }
    fun setMiniPlayerEnabled(value: Float) = update { it.copy(miniPlayerEnabled = value.coerceIn(0f, 1f)) }
    fun setNavigationEnabled(value: Float) = update { it.copy(navigationEnabled = value.coerceIn(0f, 1f)) }
    fun setReflectionIntensity(value: Float) = update { it.copy(reflectionIntensity = value.coerceIn(0f, 2f)) }
    fun setBlurTint(value: Color) = update { it.copy(blurTint = value) }

    fun reset() {
        _uiState.value = LiquidGlassSettings()
        persist(_uiState.value)
    }

    fun exportToSyncPayload(): JsonObject = buildJsonObject {
        val state = uiState.value
        put("liquid_glass_enabled", encodeSyncBoolean(state.enabled))
        put("enhanced_liquid_glass", encodeSyncBoolean(state.enhancedLiquidGlass))
        put("liquid_glass_vibrancy", encodeSyncFloat(state.vibrancy))
        put("liquid_glass_blur_radius", encodeSyncFloat(state.blurRadius))
        put("liquid_glass_refraction_height", encodeSyncFloat(state.refractionHeight))
        put("liquid_glass_refraction_amount", encodeSyncFloat(state.refractionAmount))
        put("liquid_glass_chromatic_aberration", encodeSyncFloat(state.chromaticAberration))
        put("liquid_glass_depth_effect", encodeSyncFloat(state.depthEffect))
        put("liquid_glass_surface_tint", encodeSyncString(encodeColor(state.surfaceTint)))
        put("liquid_glass_surface_opacity", encodeSyncFloat(state.surfaceOpacity))
        put("liquid_glass_text_color", encodeSyncString(encodeColor(state.textColor)))
        put("liquid_glass_rim_intensity", encodeSyncFloat(state.rimIntensity))
        put("liquid_glass_rim_width", encodeSyncFloat(state.rimWidth))
        put("liquid_glass_reflection_angle", encodeSyncFloat(state.reflectionAngle))
        put("liquid_glass_player_enabled", encodeSyncFloat(state.playerEnabled))
        put("liquid_glass_mini_player_enabled", encodeSyncFloat(state.miniPlayerEnabled))
        put("liquid_glass_navigation_enabled", encodeSyncFloat(state.navigationEnabled))
        put("liquid_glass_reflection_intensity", encodeSyncFloat(state.reflectionIntensity))
        put("liquid_glass_blur_tint", encodeSyncString(encodeColor(state.blurTint)))
    }

    fun replaceFromSyncPayload(payload: JsonObject) {
        payload.decodeSyncBoolean("liquid_glass_enabled")?.let(::setEnabled)
        payload.decodeSyncBoolean("enhanced_liquid_glass")?.let(::setEnhancedLiquidGlass)
        payload.decodeSyncFloat("liquid_glass_vibrancy")?.let(::setVibrancy)
        payload.decodeSyncFloat("liquid_glass_blur_radius")?.let(::setBlurRadius)
        payload.decodeSyncFloat("liquid_glass_refraction_height")?.let(::setRefractionHeight)
        payload.decodeSyncFloat("liquid_glass_refraction_amount")?.let(::setRefractionAmount)
        payload.decodeSyncFloat("liquid_glass_chromatic_aberration")?.let(::setChromaticAberration)
        payload.decodeSyncFloat("liquid_glass_depth_effect")?.let(::setDepthEffect)
        payload.decodeSyncString("liquid_glass_surface_tint")?.let { decodeColor(it)?.let(::setSurfaceTint) }
        payload.decodeSyncFloat("liquid_glass_surface_opacity")?.let(::setSurfaceOpacity)
        payload.decodeSyncString("liquid_glass_text_color")?.let { decodeColor(it)?.let(::setTextColor) }
        payload.decodeSyncFloat("liquid_glass_rim_intensity")?.let(::setRimIntensity)
        payload.decodeSyncFloat("liquid_glass_rim_width")?.let(::setRimWidth)
        payload.decodeSyncFloat("liquid_glass_reflection_angle")?.let(::setReflectionAngle)
        payload.decodeSyncFloat("liquid_glass_player_enabled")?.let(::setPlayerEnabled)
        payload.decodeSyncFloat("liquid_glass_mini_player_enabled")?.let(::setMiniPlayerEnabled)
        payload.decodeSyncFloat("liquid_glass_navigation_enabled")?.let(::setNavigationEnabled)
        payload.decodeSyncFloat("liquid_glass_reflection_intensity")?.let(::setReflectionIntensity)
        payload.decodeSyncString("liquid_glass_blur_tint")?.let { decodeColor(it)?.let(::setBlurTint) }
    }

    private inline fun update(transform: (LiquidGlassSettings) -> LiquidGlassSettings) {
        ensureLoaded()
        val next = transform(_uiState.value)
        _uiState.value = next
        persist(next)
    }

    private fun persist(state: LiquidGlassSettings) {
        ThemeSettingsStorage.saveLiquidGlassEnabled(state.enabled)
        ThemeSettingsStorage.saveEnhancedLiquidGlass(state.enhancedLiquidGlass)
        ThemeSettingsStorage.saveLiquidGlassVibrancy(state.vibrancy)
        ThemeSettingsStorage.saveLiquidGlassBlurRadius(state.blurRadius)
        ThemeSettingsStorage.saveLiquidGlassRefractionHeight(state.refractionHeight)
        ThemeSettingsStorage.saveLiquidGlassRefractionAmount(state.refractionAmount)
        ThemeSettingsStorage.saveLiquidGlassChromaticAberration(state.chromaticAberration)
        ThemeSettingsStorage.saveLiquidGlassDepthEffect(state.depthEffect)
        ThemeSettingsStorage.saveLiquidGlassSurfaceTint(encodeColor(state.surfaceTint))
        ThemeSettingsStorage.saveLiquidGlassSurfaceOpacity(state.surfaceOpacity)
        ThemeSettingsStorage.saveLiquidGlassTextColor(encodeColor(state.textColor))
        ThemeSettingsStorage.saveLiquidGlassRimIntensity(state.rimIntensity)
        ThemeSettingsStorage.saveLiquidGlassRimWidth(state.rimWidth)
        ThemeSettingsStorage.saveLiquidGlassReflectionAngle(state.reflectionAngle)
        ThemeSettingsStorage.saveLiquidGlassPlayerEnabled(state.playerEnabled)
        ThemeSettingsStorage.saveLiquidGlassMiniPlayerEnabled(state.miniPlayerEnabled)
        ThemeSettingsStorage.saveLiquidGlassNavigationEnabled(state.navigationEnabled)
        ThemeSettingsStorage.saveLiquidGlassReflectionIntensity(state.reflectionIntensity)
        ThemeSettingsStorage.saveLiquidGlassBlurTint(encodeColor(state.blurTint))
    }

    private fun encodeColor(color: Color): String = color.value.toString(16)

    private fun decodeColor(value: String?): Color? = runCatching {
        Color(value?.removePrefix("#")?.toULong(16) ?: return null)
    }.getOrNull()
}
