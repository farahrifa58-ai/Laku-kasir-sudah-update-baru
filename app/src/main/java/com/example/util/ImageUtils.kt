package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Copies an image from a content URI into app's private internal storage
     * so it never expires when the app is closed or restarted.
     * Returns the absolute file path.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri, filenamePrefix: String): String? {
        return try {
            val imagesDir = File(context.filesDir, "app_images").apply { mkdirs() }
            val destFile = File(imagesDir, "${filenamePrefix}_${System.currentTimeMillis()}.jpg")

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap != null) {
                    val fos = FileOutputStream(destFile)
                    // Scale down slightly if massive to preserve memory & fast loading
                    val scaled = if (bitmap.width > 1200 || bitmap.height > 1200) {
                        val ratio = minOf(1200f / bitmap.width, 1200f / bitmap.height)
                        Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * ratio).toInt(),
                            (bitmap.height * ratio).toInt(),
                            true
                        )
                    } else {
                        bitmap
                    }
                    scaled.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                    fos.flush()
                    fos.close()
                    destFile.absolutePath
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads a Bitmap from an absolute file path or Uri string safely
     */
    fun loadBitmap(context: Context, pathOrUri: String?): Bitmap? {
        if (pathOrUri.isNullOrBlank()) return null
        return try {
            val file = File(pathOrUri)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                val uri = Uri.parse(pathOrUri)
                val stream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(stream)
                stream?.close()
                bmp
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
