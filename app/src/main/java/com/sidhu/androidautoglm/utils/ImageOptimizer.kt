package com.sidhu.androidautoglm.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import java.io.ByteArrayOutputStream

object ImageOptimizer {
    private const val TAG = "ImageOptimizer"
    
    // 目标宽度 - 保持合理的分辨率同时减少大小
    private const val TARGET_WIDTH = 720
    private const val TARGET_HEIGHT = 1280
    
    // 质量设置
    private const val HIGH_QUALITY = 85
    private const val NORMAL_QUALITY = 75
    private const val LOW_QUALITY = 60
    
    data class OptimizationResult(
        val optimizedBitmap: Bitmap,
        val originalSize: Long,
        val optimizedSize: Long,
        val compressionRatio: Float,
        val resized: Boolean
    )
    
    /**
     * 智能优化截图
     * @param bitmap 原始截图
     * @param targetQuality 目标质量 (HIGH_QUALITY, NORMAL_QUALITY, LOW_QUALITY)
     * @return 优化结果
     */
    fun optimizeScreenshot(
        bitmap: Bitmap,
        targetQuality: Int = NORMAL_QUALITY
    ): OptimizationResult {
        val startTime = System.currentTimeMillis()
        val originalSize = estimateBitmapSize(bitmap)
        
        // 1. 检查是否需要缩放
        var optimizedBitmap = bitmap
        var needsResize = false
        
        if (bitmap.width > TARGET_WIDTH || bitmap.height > TARGET_HEIGHT) {
            optimizedBitmap = resizeBitmap(bitmap, TARGET_WIDTH, TARGET_HEIGHT)
            needsResize = true
            Log.d(TAG, "Resized from ${bitmap.width}x${bitmap.height} to ${optimizedBitmap.width}x${optimizedBitmap.height}")
        }
        
        // 2. 压缩质量
        val compressedBitmap = compressBitmap(optimizedBitmap, targetQuality)
        val optimizedSize = estimateBitmapSize(compressedBitmap)
        
        val compressionRatio = (originalSize - optimizedSize).toFloat() / originalSize.toFloat()
        val duration = System.currentTimeMillis() - startTime
        
        Log.i(TAG, "Optimization completed in ${duration}ms")
        Log.i(TAG, "Original: ${formatBytes(originalSize)}, Optimized: ${formatBytes(optimizedSize)}")
        Log.i(TAG, "Compression ratio: ${String.format("%.1f%%", compressionRatio * 100)}")
        
        return OptimizationResult(
            optimizedBitmap = compressedBitmap,
            originalSize = originalSize,
            optimizedSize = optimizedSize,
            compressionRatio = compressionRatio,
            resized = needsResize
        )
    }
    
    /**
     * 智能调整图片大小，保持宽高比
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // 计算缩放比例
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        
        // 使用高质量的缩放算法
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
    
    /**
     * 压缩Bitmap质量
     */
    private fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        
        // 如果压缩后更大，返回原图
        if (byteArray.size > estimateBitmapSize(bitmap)) {
            return bitmap
        }
        
        return android.graphics.BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
    
    /**
     * 估算Bitmap大小（字节）
     */
    private fun estimateBitmapSize(bitmap: Bitmap): Long {
        return (bitmap.width * bitmap.height * 4).toLong() // ARGB_8888 = 4 bytes per pixel
    }
    
    /**
     * 根据当前内存状态自动选择压缩质量
     */
    fun getAdaptiveQuality(currentMemoryMB: Float): Int {
        return when {
            currentMemoryMB > 400 -> LOW_QUALITY      // 高内存压力，使用低质量
            currentMemoryMB > 300 -> NORMAL_QUALITY   // 中等压力，使用正常质量
            else -> HIGH_QUALITY                       // 内存充足，使用高质量
        }
    }
    
    /**
     * 批量优化多张图片
     */
    fun optimizeBatch(bitmaps: List<Bitmap>, quality: Int = NORMAL_QUALITY): List<Bitmap> {
        return bitmaps.map { bitmap ->
            optimizeScreenshot(bitmap, quality).optimizedBitmap
        }
    }
    
    /**
     * 格式化字节大小显示
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> String.format("%.2f MB", bytes / 1024.0 / 1024.0)
        }
    }
    
    /**
     * 获取压缩统计信息
     */
    fun getCompressionStats(originalSize: Long, optimizedSize: Long): String {
        val saved = originalSize - optimizedSize
        val ratio = if (originalSize > 0) (saved.toFloat() / originalSize.toFloat() * 100) else 0f
        return "节省 ${formatBytes(saved)} (${String.format("%.1f%%", ratio)})"
    }
}
