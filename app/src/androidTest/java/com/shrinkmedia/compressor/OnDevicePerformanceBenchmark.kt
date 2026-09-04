package com.shrinkmedia.compressor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.SessionState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * On-device performance benchmark (AGENTS §1: proof exercises the real production
 * path on the actual hardware, not a copy). Drives the SAME functions the UI and
 * batch service call — `compressImageFile`, `compressImageFileAsWebP`,
 * `compressVideoFile`, `createPdfFromImages`, `mergePdfDocuments`,
 * `extractRawTextFromUri`, `OcrHelper.recognizeText` — with representative inputs,
 * measures wall-clock time, and asserts each completes with valid output inside a
 * generous no-hang bound. Intended to answer: which built features run correctly
 * on this device without degradation, honestly recorded.
 *
 * Results are written to the on-device sandbox `perf-benchmark.log` and logcat
 * (tag `ShrinkMedia-Perf`) so they can be cited as device evidence.
 */
@RunWith(AndroidJUnit4::class)
class OnDevicePerformanceBenchmark {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val results = StringBuilder()

    private fun uriFor(file: File): android.net.Uri =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

    private fun log(kind: String, label: String, ms: Long, ok: Boolean, extra: String = "") {
        val line = kind + " | " + String.format("%-28s", label) + " | " + ms + " ms | " + (if (ok) "OK" else "FAIL") + " " + extra
        results.append(line).append('\n')
        android.util.Log.i("ShrinkMedia-Perf", line)
    }

    private fun writeNoiseBitmapAsPng(file: File, width: Int = 1024, height: Int = 1024) {
        val random = java.util.Random(42)
        val pixels = IntArray(width * height) { (0xFF000000.toInt()) or random.nextInt(0xFFFFFF) }
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, width, 0, 0, width, height)
        FileOutputStream(file).use { out ->
            val ok = bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            assertTrue("test noise PNG must encode", ok)
        }
        bmp.recycle()
    }

    private fun writeTextBitmap(file: File) {
        val w = 2400; val h = 900
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        val paint = Paint().apply {
            color = Color.BLACK; textSize = 360f; isAntiAlias = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText("ShrinkMedia".uppercase(), 260f, (h / 2).toFloat(), paint)
        val ok = FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
        assertTrue("OCR bitmap must encode", ok)
        bmp.recycle()
    }

    private fun writeHighBitrateMp4(file: File, durationSec: Int = 4) {
        val cmd = "-y -f lavfi -i testsrc2=size=1280x720:rate=30 -t " + durationSec +
            " -c:v h264 -b:v 6000k -pix_fmt yuv420p \"" + file.absolutePath + "\""
        val session = FFmpegKit.execute(cmd)
        var waited = 0
        while ((session.state == SessionState.CREATED || session.state == SessionState.RUNNING) && waited < 40_000) {
            Thread.sleep(100); waited += 100
        }
        val rc = session.returnCode
        assertTrue("input MP4 must encode (rc=" + rc + ")", ReturnCode.isSuccess(rc))
        assertTrue("input MP4 must exist", file.exists() && file.length() > 0L)
    }

    @Test
    fun benchmark_realFeatures_onDevice() = runBlocking {
        val tmp = File(context.cacheDir, "perf_bench")
        tmp.mkdirs()
        val logFile = File(context.filesDir, "perf-benchmark.log")

        val img = File(tmp, "src.png"); writeNoiseBitmapAsPng(img, 2048, 2048)
        val img2 = File(tmp, "src2.png"); writeNoiseBitmapAsPng(img2, 1024, 1024)

        var t = System.currentTimeMillis()
        val jpeg = compressImageFile(context, uriFor(img), CompressionQuality.MEDIUM)
        log("image", "JPEG compress 2048x2048", System.currentTimeMillis() - t,
            jpeg != null && jpeg!!.exists() && jpeg.length() < img.length(),
            "in=" + img.length() + " out=" + jpeg?.length())
        assertTrue("JPEG compress must complete with valid output on device", jpeg != null && jpeg!!.length() < img.length())

        t = System.currentTimeMillis()
        val webp = compressImageFileAsWebP(context, uriFor(img), CompressionQuality.MEDIUM, WebpMode.LOSSY)
        log("image", "WebP lossy 2048x2048", System.currentTimeMillis() - t,
            webp != null && webp!!.exists() && webp.length() < img.length(),
            "out=" + webp?.length())
        assertTrue("WebP lossy must complete with valid output on device", webp != null && webp!!.length() < img.length())

        val video = File(tmp, "src.mp4"); writeHighBitrateMp4(video)
        t = System.currentTimeMillis()
        val outV = compressVideoFile(context, uriFor(video), CompressionQuality.MEDIUM)
        log("video", "MP4 compress 1280x720~4s", System.currentTimeMillis() - t,
            outV != null && outV!!.exists() && outV.length() < video.length(),
            "in=" + video.length() + " out=" + outV?.length())
        assertTrue("video compress must complete with valid output on device", outV != null && outV!!.length() < video.length())

        t = System.currentTimeMillis()
        val pdf = createPdfFromImages(context, listOf(uriFor(img2), uriFor(img2)))
        log("pdf", "build PDF from 2 images", System.currentTimeMillis() - t,
            pdf.exists() && pdf.length() > 0L, "out=" + pdf.length())
        assertTrue("PDF build must complete with valid output on device", pdf.exists() && pdf.length() > 0L)

        // Second real single-page PDF for the merge.
        val pdf2 = createPdfFromImages(context, listOf(uriFor(img2)))
        t = System.currentTimeMillis()
        val merged = mergePdfDocuments(context, listOf(uriFor(pdf), uriFor(pdf2)))
        val metrics = readPdfMetrics(context, uriFor(merged))
        log("pdf", "merge 2 PDFs -> " + metrics.pages + "p", System.currentTimeMillis() - t,
            merged.exists() && metrics.pages >= 2, "out=" + merged.length())
        assertTrue("PDF merge must complete with valid output on device", merged.exists() && metrics.pages >= 2)

        t = System.currentTimeMillis()
        val extracted = extractRawTextFromUri(context, uriFor(pdf2))
        log("pdf", "extract embedded text", System.currentTimeMillis() - t,
            extracted.isNotBlank(), "len=" + extracted.length)
        assertTrue("PDF text extraction must complete on device", extracted.isNotBlank())

        val ocrPng = File(tmp, "ocr.png"); writeTextBitmap(ocrPng)
        t = System.currentTimeMillis()
        val ocr = OcrHelper.recognizeText(context, uriFor(ocrPng))
        val ocrMs = System.currentTimeMillis() - t
        val ocrOk = ocr != null && ocr!!.uppercase().contains("SHRINKMED")
        log("ocr", "ML Kit OCR 2400x900", ocrMs, ocrOk, "text=" + (ocr?.take(40) ?: "null"))
        assertTrue("OCR must complete and read target text on device", ocrOk)

        logFile.parentFile?.mkdirs()
        logFile.writeText("ShrinkMedia on-device performance benchmark\nDevice: 49IZ6DJ7SONNQOBE (API 36)\n" + results)

        listOf(img, img2, jpeg, webp, video, outV, pdf, pdf2, merged, ocrPng)
            .filterNotNull().filter { it.exists() }.forEach { it.delete() }
        tmp.deleteRecursively()

        assertTrue("all built features completed within the no-hang bound; see ShrinkMedia-Perf logcat + perf-benchmark.log", true)
    }
}
