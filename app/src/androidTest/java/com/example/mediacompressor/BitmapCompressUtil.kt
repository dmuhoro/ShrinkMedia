package com.example.mediacompressor

import android.graphics.Bitmap
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Test support only: writes a Bitmap as an uncompressed 24-bit BMP so
 * instrumented tests get a deterministic, uncompressed input size that is
 * guaranteed larger than any real JPEG encoding of the same scene.
 */
internal object BitmapCompressUtil {
    fun writeBmp(bitmap: Bitmap, file: File) {
        val width = bitmap.width
        val height = bitmap.height
        val rowPadding = (4 - (width * 3) % 4) % 4
        val pixelRowSize = width * 3 + rowPadding
        val dataSize = pixelRowSize * height
        val fileSize = 54 + dataSize

        BufferedOutputStream(FileOutputStream(file)).use { out ->
            // BITMAPFILEHEADER
            out.write('B'.code); out.write('M'.code)                      // bfType
            writeLittleEndianInt(out, fileSize)                            // bfSize
            writeLittleEndianShort(out, 0)                                 // bfReserved1
            writeLittleEndianShort(out, 0)                                 // bfReserved2
            writeLittleEndianInt(out, 54)                                  // bfOffBits
            // BITMAPINFOHEADER
            writeLittleEndianInt(out, 40)                                  // biSize
            writeLittleEndianInt(out, width)                               // biWidth
            writeLittleEndianInt(out, height)                              // biHeight
            writeLittleEndianShort(out, 1)                                 // biPlanes
            writeLittleEndianShort(out, 24)                                // biBitCount

            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val row = ByteArray(pixelRowSize)
            for (y in height - 1 downTo 0) {
                for (x in 0 until width) {
                    val argb = pixels[y * width + x]
                    row[x * 3] = (argb and 0xFF).toByte()          // B
                    row[x * 3 + 1] = ((argb shr 8) and 0xFF).toByte()  // G
                    row[x * 3 + 2] = ((argb shr 16) and 0xFF).toByte() // R
                }
                out.write(row)
            }
            out.flush()
        }
    }

    private fun writeLittleEndianInt(out: java.io.OutputStream, value: Int) {
        out.write(value and 0xFF)
        out.write((value shr 8) and 0xFF)
        out.write((value shr 16) and 0xFF)
        out.write((value shr 24) and 0xFF)
    }

    private fun writeLittleEndianShort(out: java.io.OutputStream, value: Int) {
        out.write(value and 0xFF)
        out.write((value shr 8) and 0xFF)
    }
}
