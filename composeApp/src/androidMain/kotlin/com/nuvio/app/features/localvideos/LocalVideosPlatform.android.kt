package com.nuvio.app.features.localvideos

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

actual object LocalVideosPlatform {
    private const val permissionRequestCode = 9417

    private var appContext: Context? = null
    private var currentActivity: WeakReference<Activity>? = null
    private var permissionChangedListener: ((Boolean) -> Unit)? = null

    actual val isSupported: Boolean
        get() = true

    actual fun initialize() {
        // Context is provided through bindActivity from MainActivity.
    }

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun bindActivity(activity: Activity) {
        if (appContext == null) {
            appContext = activity.applicationContext
        }
        currentActivity = WeakReference(activity)
    }

    fun unbindActivity(activity: Activity) {
        if (currentActivity?.get() === activity) {
            currentActivity = null
        }
    }

    private fun requiredPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    actual fun hasVideoPermission(): Boolean {
        val context = appContext ?: return false
        val granted = ContextCompat.checkSelfPermission(
            context,
            requiredPermission(),
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ partial photo/video access still lets us read the user-selected items.
            val partial = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            ) == PackageManager.PERMISSION_GRANTED
            if (partial) return true
        }
        return granted
    }

    actual fun requestVideoPermission() {
        val activity = currentActivity?.get() ?: return
        val permissions = mutableListOf(requiredPermission())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions += Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        }
        ActivityCompat.requestPermissions(
            activity,
            permissions.toTypedArray(),
            permissionRequestCode,
        )
    }

    actual fun setPermissionChangedListener(listener: ((Boolean) -> Unit)?) {
        permissionChangedListener = listener
    }

    fun handlePermissionRequestResult(requestCode: Int): Boolean {
        if (requestCode != permissionRequestCode) return false
        permissionChangedListener?.invoke(hasVideoPermission())
        return true
    }

    actual suspend fun queryLocalVideos(): List<LocalVideoItem> = withContext(Dispatchers.IO) {
        val context = appContext ?: return@withContext emptyList()
        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
        )
        val items = mutableListOf<LocalVideoItem>()
        runCatching {
            resolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC",
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val rawName = cursor.getString(nameColumn).orEmpty()
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id,
                    )
                    items += LocalVideoItem(
                        id = id.toString(),
                        title = rawName.substringBeforeLast('.').ifBlank { rawName },
                        contentUri = contentUri.toString(),
                        durationMs = runCatching { cursor.getLong(durationColumn) }.getOrDefault(0L),
                        sizeBytes = runCatching { cursor.getLong(sizeColumn) }.getOrDefault(0L),
                        dateAddedEpochSeconds = runCatching { cursor.getLong(dateColumn) }.getOrDefault(0L),
                        width = runCatching { cursor.getInt(widthColumn) }.getOrDefault(0),
                        height = runCatching { cursor.getInt(heightColumn) }.getOrDefault(0),
                    )
                }
            }
        }
        items
    }

    actual suspend fun loadThumbnail(
        video: LocalVideoItem,
        targetWidthPx: Int,
        targetHeightPx: Int,
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        val context = appContext ?: return@withContext null
        val width = targetWidthPx.takeIf { it > 0 } ?: 384
        val height = targetHeightPx.takeIf { it > 0 } ?: 216
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(
                    Uri.parse(video.contentUri),
                    Size(width, height),
                    null,
                )?.asImageBitmap()
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Video.Thumbnails.getThumbnail(
                    context.contentResolver,
                    video.id.toLongOrNull() ?: return@runCatching null,
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    null,
                )?.asImageBitmap()
            }
        }.getOrNull() ?: loadThumbnailWithRetriever(context, video)
    }

    private fun loadThumbnailWithRetriever(
        context: Context,
        video: LocalVideoItem,
    ): ImageBitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(video.contentUri))
            retriever.frameAtTime?.asImageBitmap()
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    actual suspend fun displayNameForUri(uriString: String): String? = withContext(Dispatchers.IO) {
        val context = appContext ?: return@withContext null
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return@withContext null
        when (uri.scheme?.lowercase()) {
            ContentResolver.SCHEME_CONTENT -> {
                val queried = runCatching {
                    context.contentResolver.query(
                        uri,
                        arrayOf(OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null,
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (index >= 0) cursor.getString(index) else null
                        } else {
                            null
                        }
                    }
                }.getOrNull()
                queried ?: uri.lastPathSegment?.substringAfterLast('/')
            }

            ContentResolver.SCHEME_FILE -> uri.lastPathSegment
            else -> {
                val segment = uri.lastPathSegment ?: return@withContext null
                runCatching { Uri.decode(segment) }.getOrDefault(segment)
            }
        }?.trim()?.takeIf { it.isNotBlank() }
    }
}
