package com.nuvio.app.features.localvideos

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object LocalVideosRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(LocalVideosUiState())
    val uiState: StateFlow<LocalVideosUiState> = _uiState.asStateFlow()

    private var listenerRegistered = false

    fun ensureLoaded() {
        if (!LocalVideosPlatform.isSupported) {
            _uiState.update {
                it.copy(isSupported = false, isLoaded = true, isLoading = false)
            }
            return
        }
        LocalVideosPlatform.initialize()
        if (!listenerRegistered) {
            listenerRegistered = true
            LocalVideosPlatform.setPermissionChangedListener { granted ->
                _uiState.update { it.copy(permissionGranted = granted) }
                if (granted) refresh()
            }
        }
        val granted = LocalVideosPlatform.hasVideoPermission()
        val state = _uiState.value
        _uiState.update { it.copy(isSupported = true, permissionGranted = granted) }
        if (granted && (!state.isLoaded || state.videos.isEmpty()) && !state.isLoading) {
            refresh()
        } else if (!granted) {
            _uiState.update { it.copy(isLoaded = true, isLoading = false) }
        }
    }

    fun refresh() {
        if (!LocalVideosPlatform.isSupported) return
        if (!LocalVideosPlatform.hasVideoPermission()) {
            _uiState.update { it.copy(permissionGranted = false, isLoaded = true, isLoading = false) }
            return
        }
        _uiState.update { it.copy(permissionGranted = true, isLoading = true) }
        scope.launch {
            val videos = runCatching { LocalVideosPlatform.queryLocalVideos() }.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    isLoaded = true,
                    isLoading = false,
                    videos = videos.sortedByDescending(LocalVideoItem::dateAddedEpochSeconds),
                )
            }
        }
    }

    fun requestPermission() {
        if (!LocalVideosPlatform.isSupported) return
        LocalVideosPlatform.requestVideoPermission()
    }
}
