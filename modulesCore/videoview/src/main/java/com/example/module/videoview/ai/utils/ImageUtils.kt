package com.example.aitrae.utils

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

object ImageUtils {
    private const val MAX_FILE_SIZE_BYTES = 7_864_320
    private const val TAG = "ImageUtils"
    
    /**
     * 压缩图片，确保其大小不超过指定的最大值（默认10MB）
     * @param bitmap 原始图片
     * @param maxSizeBytes 最大文件大小（字节）
     * @return 压缩后的图片
     */
    fun compressBitmap(bitmap: Bitmap, maxSizeBytes: Int = MAX_FILE_SIZE_BYTES): Bitmap {
        var quality = 100
        var compressedBitmap = bitmap
        var byteArrayOutputStream = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        
        // 如果图片已经小于最大大小，直接返回
        if (byteArrayOutputStream.size() <= maxSizeBytes) {
            Log.d(TAG, "图片大小已经符合要求：${byteArrayOutputStream.size()} bytes")
            return compressedBitmap
        }
        
        // 首先尝试降低质量
        while (byteArrayOutputStream.size() > maxSizeBytes && quality > 10) {
            byteArrayOutputStream.reset()
            quality -= 10
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            Log.d(TAG, "压缩质量：$quality，大小：${byteArrayOutputStream.size()} bytes")
        }
        
        // 如果降低质量后仍然太大，则缩小尺寸
        var scale = 1.0f
        while (byteArrayOutputStream.size() > maxSizeBytes && scale > 0.1f) {
            scale -= 0.1f
            val newWidth = (compressedBitmap.width * scale).toInt()
            val newHeight = (compressedBitmap.height * scale).toInt()
            
            // 确保尺寸不为0
            if (newWidth <= 0 || newHeight <= 0) break
            
            val scaledBitmap = Bitmap.createScaledBitmap(compressedBitmap, newWidth, newHeight, true)
            
            // 如果不是原始图片，释放之前的缩放图片
            if (compressedBitmap != bitmap) {
                compressedBitmap.recycle()
            }
            
            compressedBitmap = scaledBitmap
            byteArrayOutputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            Log.d(TAG, "缩放比例：$scale，大小：${byteArrayOutputStream.size()} bytes")
        }
        
        Log.d(TAG, "最终图片大小：${byteArrayOutputStream.size()} bytes")
        return compressedBitmap
    }
    
    /**
     * 将Bitmap转换为Base64编码的字符串
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val compressedBitmap = compressBitmap(bitmap, MAX_FILE_SIZE_BYTES)
        val byteArrayOutputStream = ByteArrayOutputStream()
        var quality = 85
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        while (byteArrayOutputStream.size() > MAX_FILE_SIZE_BYTES && quality > 30) {
            byteArrayOutputStream.reset()
            quality -= 10
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        }
        val byteArray = byteArrayOutputStream.toByteArray()
        if (compressedBitmap != bitmap) {
            compressedBitmap.recycle()
        }
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * 将Bitmap转换为Base64编码的Data URL
     */
    fun bitmapToBase64DataUrl(bitmap: Bitmap): String {
        val base64 = bitmapToBase64(bitmap)
        return "data:image/jpeg;base64,$base64"
    }
}
