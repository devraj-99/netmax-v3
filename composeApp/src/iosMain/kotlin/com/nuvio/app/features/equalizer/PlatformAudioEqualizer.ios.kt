package com.nuvio.app.features.equalizer

actual object PlatformAudioEqualizer {
    actual val supportsVolumeBoost: Boolean = false
    actual fun applyState(state: EqualizerState) {}
    actual fun attachAudioSession(sessionId: Int) {}
    actual fun detachAudioSession(sessionId: Int) {}
}
