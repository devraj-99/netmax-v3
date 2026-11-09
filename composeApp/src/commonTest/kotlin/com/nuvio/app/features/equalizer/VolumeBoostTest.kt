package com.nuvio.app.features.equalizer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VolumeBoostTest {
    @Test
    fun boostGainUsesLinearAmplitudeAndClampsToSupportedRange() {
        assertEquals(0, VolumeBoost.gainMillibels(100))
        assertEquals(352, VolumeBoost.gainMillibels(150))
        assertEquals(602, VolumeBoost.gainMillibels(200))
        assertEquals(0, VolumeBoost.gainMillibels(Int.MIN_VALUE))
        assertEquals(602, VolumeBoost.gainMillibels(Int.MAX_VALUE))
        assertEquals(6.0206, VolumeBoost.gainDb(200), 0.0001)
        assertTrue((100..200).zipWithNext().all { (a, b) ->
            VolumeBoost.gainMillibels(a) < VolumeBoost.gainMillibels(b)
        })
    }

    @Test
    fun boostIsIndependentOfEqToggleAndPresetsAndResetReturnsToNormal() {
        EqualizerSettingsRepository.reset()
        EqualizerSettingsRepository.setEnabled(false)
        EqualizerSettingsRepository.setVolumeBoost(200)
        assertEquals(200, EqualizerSettingsRepository.state.value.volumeBoostPercent)
        EqualizerSettingsRepository.applyPreset(EqualizerPreset.Voice)
        assertEquals(200, EqualizerSettingsRepository.state.value.volumeBoostPercent)
        EqualizerSettingsRepository.setEnabled(true)
        EqualizerSettingsRepository.setEnabled(false)
        assertEquals(200, EqualizerSettingsRepository.state.value.volumeBoostPercent)
        EqualizerSettingsRepository.setVolumeBoost(500)
        assertEquals(200, EqualizerSettingsRepository.state.value.volumeBoostPercent)
        EqualizerSettingsRepository.setVolumeBoost(-1)
        assertEquals(100, EqualizerSettingsRepository.state.value.volumeBoostPercent)
        EqualizerSettingsRepository.setVolumeBoost(150)
        EqualizerSettingsRepository.reset()
        assertEquals(100, EqualizerSettingsRepository.state.value.volumeBoostPercent)
    }
}
