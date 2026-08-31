package com.shrinkmedia.compressor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * On-device OCR (Google ML Kit, bundled Latin model). Runs completely locally —
 * no INTERNET permission, no uploads (ADR-009).
 *
 * Typed-null contract (Constitution Article III): returns recognized text on
 * success, or `null` on failure — callers must surface the failure explicitly,
 * never return an empty string pretending success (I.6).
 */
object OcrHelper {

    /** Maximum image dimension fed to the recognizer (bounds memory, keeps
     *  accuracy reasonable without over-scaling huge photos). */
    private const val MAX_DIMENSION_PX = 2560

    /**
     * Recognize text in the image referenced by [imageUri].
     *
     * Returns the recognized text on success (may be empty when no text found),
     * or `null` when OCR could not run (failed to decode / recognize).
     */
    suspend fun recognizeText(context: Context, imageUri: Uri): String? {
        val recognizer: TextRecognizer = try {
            // DEFAULT_OPTIONS = bundled Latin model; no downloads, fully on-device.
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } catch (e: Exception) {
            return null
        }
        return try {
            withContext(Dispatchers.IO) {
                val bitmap = decodeBounded(context, imageUri) ?: return@withContext null
                try {
                    val image = InputImage.fromBitmap(bitmap, 0)
                    awaitRecognition(recognizer, image).text.trim()
                } finally {
                    bitmap.recycle()
                }
            }
        } catch (e: Exception) {
            null
        } finally {
            recognizer.close()
        }
    }

    private suspend fun awaitRecognition(
        recognizer: TextRecognizer,
        image: InputImage
    ): com.google.mlkit.vision.text.Text = suspendCancellableCoroutine { cont ->
        recognizer.process(image)
            .addOnSuccessListener { result -> cont.resume(result) }
            .addOnFailureListener { e ->
                if (cont.isActive) cont.resumeWith(kotlin.Result.failure(e))
            }
    }

    private fun decodeBounded(context: Context, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / sample > MAX_DIMENSION_PX ||
               bounds.outHeight / sample > MAX_DIMENSION_PX) {
            sample *= 2
        }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        }
    }
}
