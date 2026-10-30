package com.nuvio.app.features.localvideos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * One-shot signal emitted by the player when a device-local video
 * ([LocalVideoContentType]) reaches its end, carrying the ended video id
 * ("local:<mediaStoreId>"). The app shell consumes it and advances to the
 * next video in the Local library list (auto-next).
 */
object LocalVideoAutoNextBus {
    private val _pendingEndedVideoId = MutableStateFlow<String?>(null)
    val pendingEndedVideoId: StateFlow<String?> = _pendingEndedVideoId.asStateFlow()

    fun emit(videoId: String) {
        _pendingEndedVideoId.value = videoId
    }

    fun consume(videoId: String) {
        if (_pendingEndedVideoId.value == videoId) {
            _pendingEndedVideoId.value = null
        }
    }
}
