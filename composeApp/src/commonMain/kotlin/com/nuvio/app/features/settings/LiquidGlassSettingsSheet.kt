package com.nuvio.app.features.settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.AppTheme
import com.nuvio.app.isIos
import com.nuvio.app.core.ui.NuvioBottomSheetActionRow
import com.nuvio.app.core.ui.NuvioBottomSheetDivider
import com.nuvio.app.core.ui.NuvioModalBottomSheet
import com.nuvio.app.core.ui.dismissNuvioBottomSheet
import com.nuvio.app.core.ui.labelRes
import com.nuvio.app.core.ui.ThemeColors
import com.nuvio.app.core.ui.accentBrush
import com.nuvio.app.core.ui.liquidGlass
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import com.nuvio.app.core.ui.glassSource
import com.nuvio.app.core.ui.rememberGlassHazeState
import com.nuvio.app.features.membership.MemberAccessRepository
import com.nuvio.app.features.membership.availableAppThemes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.*
import nuvio.composeapp.generated.resources.cd_selected
import nuvio.composeapp.generated.resources.collections_header
import nuvio.composeapp.generated.resources.compose_settings_page_continue_watching
import nuvio.composeapp.generated.resources.compose_settings_page_homescreen
import nuvio.composeapp.generated.resources.compose_settings_page_meta_screen
import nuvio.composeapp.generated.resources.compose_settings_page_poster_customization
import nuvio.composeapp.generated.resources.compose_settings_page_streams
import nuvio.composeapp.generated.resources.settings_appearance_app_language
import nuvio.composeapp.generated.resources.settings_appearance_app_language_sheet_title
import nuvio.composeapp.generated.resources.settings_appearance_app_icon
import nuvio.composeapp.generated.resources.settings_appearance_nav_bar_style
import nuvio.composeapp.generated.resources.settings_appearance_nav_bar_style_sheet_title
import nuvio.composeapp.generated.resources.settings_appearance_amoled_black
import nuvio.composeapp.generated.resources.settings_appearance_amoled_description
import nuvio.composeapp.generated.resources.settings_appearance_continue_watching_description
import nuvio.composeapp.generated.resources.settings_appearance_liquid_glass
import nuvio.composeapp.generated.resources.settings_appearance_liquid_glass_description
import nuvio.composeapp.generated.resources.settings_appearance_poster_customization_description
import nuvio.composeapp.generated.resources.settings_appearance_section_detail_page
import nuvio.composeapp.generated.resources.settings_appearance_section_display
import nuvio.composeapp.generated.resources.settings_appearance_section_home
import nuvio.composeapp.generated.resources.settings_appearance_section_streams
import nuvio.composeapp.generated.resources.settings_appearance_section_theme
import nuvio.composeapp.generated.resources.settings_content_discovery_collections_description
import nuvio.composeapp.generated.resources.settings_content_discovery_homescreen_description
import nuvio.composeapp.generated.resources.settings_content_discovery_meta_screen_description
import nuvio.composeapp.generated.resources.compose_settings_root_streams_description
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LiquidGlassControlsBottomSheet(
    isTablet: Boolean,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    LiquidGlassSettingsRepository.ensureLoaded()
    val state by LiquidGlassSettingsRepository.uiState.collectAsStateWithLifecycle()

    NuvioModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                dismissNuvioBottomSheet(sheetState = sheetState, onDismiss = onDismiss)
            }
        },
        sheetState = sheetState,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Text(
                        text = "Liquid Glass (Beta)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.settings_appearance_liquid_glass_controls_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_appearance_liquid_glass_preview),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    LiquidGlassLivePreview(state)
                }
            }
            item {
                SettingsSwitchRow(
                    title = stringResource(Res.string.settings_appearance_enhanced_liquid_glass),
                    description = stringResource(Res.string.settings_appearance_enhanced_liquid_glass_description),
                    checked = state.enhancedLiquidGlass,
                    isTablet = isTablet,
                    onCheckedChange = LiquidGlassSettingsRepository::setEnhancedLiquidGlass,
                )
            }
            item {
                var diag by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf("")
                }
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    while (true) {
                        diag = com.nuvio.app.core.ui.GlassDiagnostics.path + "\n" + com.nuvio.app.core.ui.GlassDiagnostics.lensStatus
                        kotlinx.coroutines.delay(1000)
                    }
                }
                Text(
                    text = diag,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { GlassSectionHeader("EFFECTS") }
            item {
                GlassSlider(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_vibrancy),
                    valueLabel = "${state.vibrancy.formatOne()}x",
                    value = state.vibrancy,
                    range = 0f..2f,
                    onValueChange = LiquidGlassSettingsRepository::setVibrancy,
                )
            }
            item {
                GlassSlider(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_blur_radius),
                    valueLabel = "${state.blurRadius.toInt()} dp",
                    value = state.blurRadius,
                    range = 0f..48f,
                    onValueChange = LiquidGlassSettingsRepository::setBlurRadius,
                )
            }
            item {
                GlassSlider(
                    title = "Rim highlight strength",
                    valueLabel = state.rimIntensity.formatOne(),
                    value = state.rimIntensity,
                    range = 0f..2f,
                    onValueChange = LiquidGlassSettingsRepository::setRimIntensity,
                )
            }
            item {
                GlassSlider(
                    title = "Rim width",
                    valueLabel = state.rimWidth.formatOne(),
                    value = state.rimWidth,
                    range = 0f..3f,
                    onValueChange = LiquidGlassSettingsRepository::setRimWidth,
                )
            }
            item {
                GlassSlider(
                    title = "Reflection light angle",
                    valueLabel = state.reflectionAngle.formatOne(),
                    value = state.reflectionAngle,
                    range = 0f..360f,
                    onValueChange = LiquidGlassSettingsRepository::setReflectionAngle,
                )
            }
            item {
                GlassSlider(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_reflection),
                    valueLabel = "${state.reflectionIntensity.formatOne()}x",
                    value = state.reflectionIntensity,
                    range = 0f..2f,
                    onValueChange = LiquidGlassSettingsRepository::setReflectionIntensity,
                )
            }
            item {
                GlassSlider(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_refraction_height),
                    valueLabel = "${(state.refractionHeight * 100).toInt()}%",
                    value = state.refractionHeight,
                    range = 0f..1f,
                    onValueChange = LiquidGlassSettingsRepository::setRefractionHeight,
                )
            }
            item {
                GlassSlider(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_refraction_amount),
                    valueLabel = "${(state.refractionAmount * 100).toInt()}%",
                    value = state.refractionAmount,
                    range = 0f..1f,
                    onValueChange = LiquidGlassSettingsRepository::setRefractionAmount,
                )
            }
            item {
                SettingsSwitchRow(
                    title = "Chromatic Aberration",
                    description = "Enable chromatic aberration",
                    checked = state.chromaticAberration > 0f,
                    isTablet = isTablet,
                    onCheckedChange = { LiquidGlassSettingsRepository.setChromaticAberration(if (it) 1f else 0f) },
                )
            }
            item {
                GlassSlider(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_chromatic_aberration),
                    valueLabel = "${(state.chromaticAberration * 100).toInt()}%",
                    value = state.chromaticAberration,
                    range = 0f..1f,
                    onValueChange = LiquidGlassSettingsRepository::setChromaticAberration,
                )
            }
            item {
                SettingsSwitchRow(
                    title = "Depth Effect",
                    description = "Enable depth effect",
                    checked = state.depthEffect > 0f,
                    isTablet = isTablet,
                    onCheckedChange = { LiquidGlassSettingsRepository.setDepthEffect(if (it) 1f else 0f) },
                )
            }
            item {
                GlassSlider(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_depth_effect),
                    valueLabel = "${(state.depthEffect * 100).toInt()}%",
                    value = state.depthEffect,
                    range = 0f..1f,
                    onValueChange = LiquidGlassSettingsRepository::setDepthEffect,
                )
            }
            item { GlassSectionHeader("APPEARANCE") }
            item {
                GlassSlider(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_surface_opacity),
                    valueLabel = "${(state.surfaceOpacity * 100).toInt()}%",
                    value = state.surfaceOpacity,
                    range = 0f..0.5f,
                    onValueChange = LiquidGlassSettingsRepository::setSurfaceOpacity,
                )
            }
            item {
                GlassColorPresets(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_surface_tint),
                    selected = state.surfaceTint,
                    colors = liquidGlassTintPresets,
                    onSelected = LiquidGlassSettingsRepository::setSurfaceTint,
                )
            }
            item {
                GlassColorPresets(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_blur_tint),
                    selected = state.blurTint,
                    colors = liquidGlassBlurTintPresets,
                    onSelected = LiquidGlassSettingsRepository::setBlurTint,
                )
            }
            item {
                GlassColorPresets(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_text_color),
                    selected = state.textColor,
                    colors = liquidGlassTextPresets,
                    onSelected = LiquidGlassSettingsRepository::setTextColor,
                )
            }
            item { GlassSectionHeader("PER COMPONENT") }
            item {
                SettingsSwitchRow(
                    title = "Glass Player",
                    description = "Use liquid glass for this component",
                    checked = state.playerEnabled > 0.5f,
                    isTablet = isTablet,
                    onCheckedChange = { LiquidGlassSettingsRepository.setPlayerEnabled(if (it) 1f else 0f) },
                )
            }
            item {
                SettingsSwitchRow(
                    title = "Glass Mini Player",
                    description = "Use liquid glass for this component",
                    checked = state.miniPlayerEnabled > 0.5f,
                    isTablet = isTablet,
                    onCheckedChange = { LiquidGlassSettingsRepository.setMiniPlayerEnabled(if (it) 1f else 0f) },
                )
            }
            item {
                SettingsSwitchRow(
                    title = "Glass Navigation Bar",
                    description = "Use liquid glass for this component",
                    checked = state.navigationEnabled > 0.5f,
                    isTablet = isTablet,
                    onCheckedChange = { LiquidGlassSettingsRepository.setNavigationEnabled(if (it) 1f else 0f) },
                )
            }
            item {
                NuvioBottomSheetActionRow(
                    title = stringResource(Res.string.settings_appearance_liquid_glass_reset),
                    onClick = LiquidGlassSettingsRepository::reset,
                )
            }
        }
    }
}

@Composable
private fun GlassSectionHeader(title: String) {
    Text(title, modifier = Modifier.padding(start = 22.dp, top = 24.dp, bottom = 10.dp),
        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun GlassSlider(
    title: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    var original by remember { mutableStateOf(value) }
    val defaults = LiquidGlassSettings()
    val defaultValue = when {
        title.contains("Vibrancy", true) -> defaults.vibrancy
        title.contains("Blur", true) -> defaults.blurRadius
        title.contains("Height", true) -> defaults.refractionHeight
        title.contains("Amount", true) -> defaults.refractionAmount
        title.contains("Opacity", true) -> defaults.surfaceOpacity
        title.contains("angle", true) -> defaults.reflectionAngle
        title.contains("width", true) -> defaults.rimWidth
        title.contains("Rim", true) -> defaults.rimIntensity
        title.contains("Reflection", true) -> defaults.reflectionIntensity
        title.contains("Chromatic", true) -> defaults.chromaticAberration
        title.contains("Depth", true) -> defaults.depthEffect
        else -> range.start
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable { original = value; open = true }.padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(valueLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (open) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { onValueChange(original); open = false },
            title = { Text(title) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value.formatOne(), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Slider(value = value, onValueChange = onValueChange, valueRange = range)
                    androidx.compose.material3.TextButton(onClick = { onValueChange(defaultValue.coerceIn(range)) }) { Text("Reset") }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { onValueChange(original); open = false }) { Text("Cancel") }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { open = false }) { Text("OK") }
            },
        )
    }
}

@Composable
private fun GlassColorPresets(
    title: String,
    selected: Color,
    colors: List<Color>,
    onSelected: (Color) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp)) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (color.alpha == 0f) MaterialTheme.colorScheme.surfaceVariant else color)
                        .border(
                            width = if (color.value == selected.value) 2.dp else 1.dp,
                            color = if (color.value == selected.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        )
                        .clickable { onSelected(color) },
                ) {
                    if (color.alpha == 0f) {
                        Canvas(modifier = Modifier.size(34.dp)) {
                            drawLine(
                                color = Color(0xFFFF5252),
                                start = Offset(6f, 6f),
                                end = Offset(size.width - 6f, size.height - 6f),
                                strokeWidth = 4f,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val liquidGlassTintPresets = listOf(
    Color.White,
    Color(0xFF9BE7FF),
    Color(0xFFC7A6FF),
    Color(0xFF9FF5C4),
    Color(0xFFFFB7D8),
    Color(0xFFFFD39A),
)

@Composable
private fun LiquidGlassLivePreview(state: LiquidGlassSettings) {
    val previewHazeState = rememberGlassHazeState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
    ) {
        // Keep the source and glass pill as siblings, so the preview never
        // includes the pill itself in the image being refracted.
        Box(Modifier.matchParentSize().glassSource(previewHazeState)) {
            Canvas(Modifier.matchParentSize()) {
                drawRect(Brush.linearGradient(listOf(
                    Color(0xFF3B1D5E), Color(0xFFD4544E),
                    Color(0xFFF0A04B), Color(0xFF2E6DA4), Color(0xFF35B8A0),
                )))
                // High-contrast lines make the actual lens displacement visible.
                val spacing = 22.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(Color.White.copy(alpha = 0.55f),
                        Offset(x, 0f), Offset(x, size.height), 2.dp.toPx())
                    x += spacing
                }
                drawCircle(Color(0xFFFFD54A), 19.dp.toPx(), Offset(33.dp.toPx(), 33.dp.toPx()))
                drawCircle(Color(0xFF8AF5FF), 15.dp.toPx(), Offset(size.width - 41.dp.toPx(), size.height / 2f))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .liquidGlass(hazeState = previewHazeState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Liquid Glass",
                style = MaterialTheme.typography.titleMedium,
                color = state.textColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .align(Alignment.BottomCenter)
                .hazeEffect(state = previewHazeState) {
                    blurRadius = (state.blurRadius * 0.625f).coerceIn(0f, 32f).dp
                    noiseFactor = 0.012f
                    if (state.blurTint.alpha > 0f) {
                        tints = listOf(HazeTint(state.blurTint))
                    }
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 0f,
                        endIntensity = 1f,
                    )
                },
        )
    }
}

private val liquidGlassBlurTintPresets = listOf(
    Color.Transparent,
    Color(0x40FFFFFF),
    Color(0x40000000),
    Color(0x404DA3FF),
    Color(0x40B44DFF),
    Color(0x40FF4D88),
)

private val liquidGlassTextPresets = listOf(
    Color.White,
    Color(0xFFF5F5F7),
    Color(0xFFBDEBFF),
    Color(0xFFE5D7FF),
    Color(0xFF17191D),
)

private fun Float.formatOne(): String = ((this * 10f).toInt() / 10f).toString()
