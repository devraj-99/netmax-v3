package com.nuvio.app.features.localvideos

import androidx.compose.ui.graphics.ImageBitmap

const val LocalVideoContentType = "local"

/**
 * A video file stored on this device, surfaced from the platform media store.
 */
data class LocalVideoItem(
    val id: String,
    val title: String,
    val contentUri: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedEpochSeconds: Long,
    val width: Int,
    val height: Int,
)

data class LocalVideosUiState(
    val isSupported: Boolean = true,
    val permissionGranted: Boolean = false,
    val isLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val videos: List<LocalVideoItem> = emptyList(),
)

/**
 * Platform bridge for listing and playing videos stored on the device.
 *
 * Android reads MediaStore.Video; other platforms report [isSupported] = false.
 */
expect object LocalVideosPlatform {
    val isSupported: Boolean

    fun initialize()

    fun hasVideoPermission(): Boolean

    /** Triggers the platform runtime permission prompt. Result arrives via [onPermissionChanged]. */
    fun requestVideoPermission()

    /** Registers a listener invoked whenever the media permission state changes. */
    fun setPermissionChangedListener(listener: ((Boolean) -> Unit)?)

    suspend fun queryLocalVideos(): List<LocalVideoItem>

    suspend fun loadThumbnail(
        video: LocalVideoItem,
        targetWidthPx: Int,
        targetHeightPx: Int,
    ): ImageBitmap?

    /** Best-effort display name for an external content/file Uri (used by "Open with" playback). */
    suspend fun displayNameForUri(uriString: String): String?
}
