package com.nuvio.app.features.localvideos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * One-shot request to play a video handed to NETMAX by another app
 * ("Open with" / VIEW intent with a video content or file Uri).
 */
data class LocalVideoOpenRequest(
    val uriString: String,
    val displayName: String?,
)

object LocalVideoOpenBus {
    private val _pendingRequest = MutableStateFlow<LocalVideoOpenRequest?>(null)
    val pendingRequest: StateFlow<LocalVideoOpenRequest?> = _pendingRequest.asStateFlow()

    fun emit(request: LocalVideoOpenRequest) {
        _pendingRequest.value = request
    }

    fun consume(request: LocalVideoOpenRequest) {
        if (_pendingRequest.value == request) {
            _pendingRequest.value = null
        }
    }
}
