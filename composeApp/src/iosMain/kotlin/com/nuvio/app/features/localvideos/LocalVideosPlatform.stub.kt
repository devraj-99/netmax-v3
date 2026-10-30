package com.nuvio.app.features.localvideos

import androidx.compose.ui.graphics.ImageBitmap

actual object LocalVideosPlatform {
    actual val isSupported: Boolean
        get() = false

    actual fun initialize() = Unit

    actual fun hasVideoPermission(): Boolean = false

    actual fun requestVideoPermission() = Unit

    actual fun setPermissionChangedListener(listener: ((Boolean) -> Unit)?) = Unit

    actual suspend fun queryLocalVideos(): List<LocalVideoItem> = emptyList()

    actual suspend fun loadThumbnail(
        video: LocalVideoItem,
        targetWidthPx: Int,
        targetHeightPx: Int,
    ): ImageBitmap? = null

    actual suspend fun displayNameForUri(uriString: String): String? = null
}
