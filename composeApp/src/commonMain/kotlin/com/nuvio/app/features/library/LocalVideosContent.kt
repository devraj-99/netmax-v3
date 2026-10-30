package com.nuvio.app.features.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nuvio.app.features.home.components.HomeEmptyStateCard
import com.nuvio.app.features.localvideos.LocalVideoItem
import com.nuvio.app.features.localvideos.LocalVideosPlatform
import com.nuvio.app.features.localvideos.LocalVideosRepository
import com.nuvio.app.features.localvideos.LocalVideosUiState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.local_videos_empty_message
import nuvio.composeapp.generated.resources.local_videos_empty_title
import nuvio.composeapp.generated.resources.local_videos_permission_action
import nuvio.composeapp.generated.resources.local_videos_permission_message
import nuvio.composeapp.generated.resources.local_videos_permission_title
import nuvio.composeapp.generated.resources.local_videos_refresh
import nuvio.composeapp.generated.resources.local_videos_unsupported_message
import nuvio.composeapp.generated.resources.local_videos_unsupported_title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.log10
import kotlin.math.pow

internal fun LazyListScope.localVideosContent(
    uiState: LocalVideosUiState,
    onVideoClick: (LocalVideoItem) -> Unit,
) {
    when {
        !uiState.isSupported -> {
            item(key = "local-videos-unsupported") {
                HomeEmptyStateCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(Res.string.local_videos_unsupported_title),
                    message = stringResource(Res.string.local_videos_unsupported_message),
                )
            }
        }

        !uiState.permissionGranted -> {
            item(key = "local-videos-permission") {
                HomeEmptyStateCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(Res.string.local_videos_permission_title),
                    message = stringResource(Res.string.local_videos_permission_message),
                    actionLabel = stringResource(Res.string.local_videos_permission_action),
                    onActionClick = { LocalVideosRepository.requestPermission() },
                )
            }
        }

        !uiState.isLoaded || uiState.isLoading -> {
            items(4, key = { "local-video-skeleton-$it" }) {
                LocalVideoRowSkeleton(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }

        uiState.videos.isEmpty() -> {
            item(key = "local-videos-empty") {
                HomeEmptyStateCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(Res.string.local_videos_empty_title),
                    message = stringResource(Res.string.local_videos_empty_message),
                    actionLabel = stringResource(Res.string.local_videos_refresh),
                    onActionClick = { LocalVideosRepository.refresh() },
                )
            }
        }

        else -> {
            item(key = "local-videos-toolbar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Smartphone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.videos.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { LocalVideosRepository.refresh() }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(Res.string.local_videos_refresh),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            items(
                count = uiState.videos.size,
                key = { index -> "local-video-${uiState.videos[index].id}" },
                contentType = { "local-video" },
            ) { index ->
                val video = uiState.videos[index]
                LocalVideoRow(
                    video = video,
                    onClick = { onVideoClick(video) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun LocalVideoRow(
    video: LocalVideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LocalVideoThumbnail(
            video = video,
            modifier = Modifier
                .width(128.dp)
                .aspectRatio(16f / 9f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildString {
                    if (video.durationMs > 0) {
                        append(formatLocalVideoDuration(video.durationMs))
                    }
                    if (video.sizeBytes > 0) {
                        if (isNotEmpty()) append("  •  ")
                        append(formatLocalVideoSize(video.sizeBytes))
                    }
                    if (video.width > 0 && video.height > 0) {
                        if (isNotEmpty()) append("  •  ")
                        append("${video.width}x${video.height}")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LocalVideoThumbnail(
    video: LocalVideoItem,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val thumbnail by produceState<ImageBitmap?>(initialValue = null, video.id) {
        val widthPx = with(density) { 128.dp.roundToPx() }
        val heightPx = with(density) { 72.dp.roundToPx() }
        value = LocalVideosPlatform.loadThumbnail(video, widthPx, heightPx)
    }
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = thumbnail
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Videocam,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colorScheme.scrim.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun LocalVideoRowSkeleton(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.surfaceContainerLow
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(128.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            )
        }
    }
}

private fun formatLocalVideoDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    fun two(value: Long): String = value.toString().padStart(2, '0')
    return if (hours > 0) {
        "$hours:${two(minutes)}:${two(seconds)}"
    } else {
        "$minutes:${two(seconds)}"
    }
}

private fun formatLocalVideoSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    val group = (log10(sizeBytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.lastIndex)
    val value = sizeBytes / 1024.0.pow(group)
    val rounded = ((value * 10).toInt() / 10.0)
    return "$rounded ${units[group]}"
}
