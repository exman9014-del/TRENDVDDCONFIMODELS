package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageDownloadHelper {
    private const val TAG = "ImageDownloadHelper"

    /**
     * Saves a list of images (drawables or URLs) directly to the device Gallery
     * under the 'NaVerdade' album using MediaStore (scoped storage compliant).
     */
    suspend fun saveImagesToGallery(
        context: Context,
        drawableResList: List<Int>,
        imageUrls: List<String>
    ): Int = withContext(Dispatchers.IO) {
        var savedCount = 0
        val total = maxOf(drawableResList.size, imageUrls.size)

        for (i in 0 until total) {
            try {
                var bitmap: Bitmap? = null
                val url = imageUrls.getOrNull(i)

                if (!url.isNullOrBlank()) {
                    if (url.startsWith("content://") || url.startsWith("file://")) {
                        try {
                            val uri = Uri.parse(url)
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                bitmap = BitmapFactory.decodeStream(inputStream)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to decode URI: $url", e)
                        }
                    } else if (url.startsWith("http")) {
                        // Load bitmap via Coil
                        val loader = ImageLoader(context)
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .allowHardware(false)
                            .build()
                        val result = loader.execute(request)
                        if (result is SuccessResult) {
                            bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                        }
                    }
                }

                if (bitmap == null) {
                    val resId = drawableResList.getOrNull(i)
                    if (resId != null && resId != 0) {
                        bitmap = BitmapFactory.decodeResource(context.resources, resId)
                    }
                }

                if (bitmap != null) {
                    val fileName = "na_verdade_${i + 1}_${System.currentTimeMillis()}.jpg"
                    val success = saveBitmapToGallery(context, bitmap!!, fileName)
                    if (success) savedCount++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving image $i to gallery", e)
            }
        }

        withContext(Dispatchers.Main) {
            if (savedCount > 0) {
                Toast.makeText(
                    context,
                    "✓ $savedCount fotos salvas na Galeria (Álbum NaVerdade)!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Não foi possível salvar as fotos na galeria.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        savedCount
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean {
        val resolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "NaVerdade")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false

            var stream: OutputStream? = null
            try {
                stream = resolver.openOutputStream(imageUri)
                if (stream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Failed writing bitmap to MediaStore", e)
                resolver.delete(imageUri, null, null)
                return false
            } finally {
                stream?.close()
            }
        } else {
            // Android 9 (API 28) and below
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val naVerdadeDir = File(picturesDir, "NaVerdade")
            if (!naVerdadeDir.exists()) naVerdadeDir.mkdirs()

            val file = File(naVerdadeDir, fileName)
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                MediaStore.Images.Media.insertImage(
                    resolver,
                    file.absolutePath,
                    fileName,
                    "Na Verdade Trend"
                )
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Failed saving bitmap legacy", e)
                return false
            }
        }
    }
}
