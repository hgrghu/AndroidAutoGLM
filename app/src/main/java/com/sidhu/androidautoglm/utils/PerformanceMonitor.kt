package com.sidhu.androidautoglm.utils

import android.os.Debug
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    
    data class TaskMetrics(
        val taskId: String,
        val startTime: Long = System.currentTimeMillis(),
        var endTime: Long? = null,
        var screenshotCount: Int = 0,
        var apiCallCount: Int = 0,
        var actionExecutionCount: Int = 0,
        var totalDataDownloaded: Long = 0,
        var memoryPeakUsage: Long = 0,
        val screenshotTimings: MutableList<Long> = mutableListOf(),
        val apiCallTimings: MutableList<Long> = mutableListOf(),
        val actionTimings: MutableList<Pair<String, Long>> = mutableListOf(),
        val memorySnapshots: MutableList<Long> = mutableListOf()
    )
    
    private val activeMetrics = ConcurrentHashMap<String, TaskMetrics>()
    
    fun startTask(taskId: String): TaskMetrics {
        val metrics = TaskMetrics(taskId = taskId)
        activeMetrics[taskId] = metrics
        Log.d(TAG, "Task started: $taskId")
        return metrics
    }
    
    fun recordScreenshot(taskId: String, bitmapSize: Int, duration: Long = 0) {
        activeMetrics[taskId]?.let { metrics ->
            metrics.screenshotCount++
            if (duration > 0) {
                metrics.screenshotTimings.add(duration)
            }
            metrics.totalDataDownloaded += bitmapSize
            updateMemoryUsage(taskId)
            Log.d(TAG, "Screenshot recorded for $taskId: count=${metrics.screenshotCount}, size=$bitmapSize bytes")
        }
    }
    
    fun recordApiCall(taskId: String, duration: Long, responseSize: Int) {
        activeMetrics[taskId]?.let { metrics ->
            metrics.apiCallCount++
            metrics.apiCallTimings.add(duration)
            metrics.totalDataDownloaded += responseSize
            updateMemoryUsage(taskId)
            Log.d(TAG, "API call recorded for $taskId: duration=${duration}ms, size=$responseSize bytes")
        }
    }
    
    fun recordAction(taskId: String, actionType: String, duration: Long) {
        activeMetrics[taskId]?.let { metrics ->
            metrics.actionExecutionCount++
            metrics.actionTimings.add(actionType to duration)
            updateMemoryUsage(taskId)
            Log.d(TAG, "Action recorded for $taskId: type=$actionType, duration=${duration}ms")
        }
    }
    
    private fun updateMemoryUsage(taskId: String) {
        activeMetrics[taskId]?.let { metrics ->
            val memoryInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(memoryInfo)
            val currentMemory = memoryInfo.totalPss * 1024L // Convert KB to bytes
            metrics.memorySnapshots.add(currentMemory)
            if (currentMemory > metrics.memoryPeakUsage) {
                metrics.memoryPeakUsage = currentMemory
            }
        }
    }
    
    fun endTask(taskId: String): TaskMetrics? {
        return activeMetrics[taskId]?.also { metrics ->
            metrics.endTime = System.currentTimeMillis()
            Log.d(TAG, "Task ended: $taskId, duration=${metrics.endTime!! - metrics.startTime}ms")
        }
    }
    
    fun getMetrics(taskId: String): TaskMetrics? {
        return activeMetrics[taskId]
    }
    
    fun clearMetrics(taskId: String) {
        activeMetrics.remove(taskId)
        Log.d(TAG, "Metrics cleared for $taskId")
    }
    
    fun generateReport(taskId: String): String {
        val metrics = activeMetrics[taskId] ?: return "No metrics found for task: $taskId"
        
        val duration = (metrics.endTime ?: System.currentTimeMillis()) - metrics.startTime
        val avgScreenshotTime = if (metrics.screenshotTimings.isNotEmpty()) {
            metrics.screenshotTimings.average()
        } else 0.0
        
        val avgApiCallTime = if (metrics.apiCallTimings.isNotEmpty()) {
            metrics.apiCallTimings.average()
        } else 0.0
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        return buildString {
            appendLine("=".repeat(50))
            appendLine("Performance Report - Task: $taskId")
            appendLine("=".repeat(50))
            appendLine("Start Time: ${dateFormat.format(Date(metrics.startTime))}")
            metrics.endTime?.let {
                appendLine("End Time: ${dateFormat.format(Date(it))}")
            }
            appendLine("Total Duration: ${duration}ms (${duration / 1000.0}s)")
            appendLine()
            appendLine("Screenshots:")
            appendLine("  Count: ${metrics.screenshotCount}")
            appendLine("  Avg Time: ${"%.2f".format(avgScreenshotTime)}ms")
            appendLine()
            appendLine("API Calls:")
            appendLine("  Count: ${metrics.apiCallCount}")
            appendLine("  Avg Time: ${"%.2f".format(avgApiCallTime)}ms")
            appendLine()
            appendLine("Actions:")
            appendLine("  Count: ${metrics.actionExecutionCount}")
            metrics.actionTimings.forEach { (type, timing) ->
                appendLine("  - $type: ${timing}ms")
            }
            appendLine()
            appendLine("Memory:")
            appendLine("  Peak Usage: ${"%.2f".format(metrics.memoryPeakUsage / 1024.0 / 1024.0)} MB")
            appendLine("  Avg Usage: ${"%.2f".format(metrics.memorySnapshots.average() / 1024.0 / 1024.0)} MB")
            appendLine()
            appendLine("Network:")
            appendLine("  Total Data: ${"%.2f".format(metrics.totalDataDownloaded / 1024.0 / 1024.0)} MB")
            appendLine("=".repeat(50))
        }
    }
    
    fun generateJsonReport(taskId: String): String {
        val metrics = activeMetrics[taskId] ?: return "{\"error\": \"No metrics found\"}"
        
        val duration = (metrics.endTime ?: System.currentTimeMillis()) - metrics.startTime
        
        val json = JSONObject().apply {
            put("taskId", metrics.taskId)
            put("startTime", metrics.startTime)
            put("endTime", metrics.endTime)
            put("duration", duration)
            put("screenshots", JSONObject().apply {
                put("count", metrics.screenshotCount)
                put("avgTime", if (metrics.screenshotTimings.isNotEmpty()) {
                    metrics.screenshotTimings.average()
                } else 0.0)
                put("timings", JSONArray(metrics.screenshotTimings))
            })
            put("apiCalls", JSONObject().apply {
                put("count", metrics.apiCallCount)
                put("avgTime", if (metrics.apiCallTimings.isNotEmpty()) {
                    metrics.apiCallTimings.average()
                } else 0.0)
                put("timings", JSONArray(metrics.apiCallTimings))
            })
            put("actions", JSONObject().apply {
                put("count", metrics.actionExecutionCount)
                val actionsArray = JSONArray()
                metrics.actionTimings.forEach { (type, timing) ->
                    actionsArray.put(JSONObject().apply {
                        put("type", type)
                        put("duration", timing)
                    })
                }
                put("executions", actionsArray)
            })
            put("memory", JSONObject().apply {
                put("peakUsageMB", metrics.memoryPeakUsage / 1024.0 / 1024.0)
                put("avgUsageMB", if (metrics.memorySnapshots.isNotEmpty()) {
                    metrics.memorySnapshots.average() / 1024.0 / 1024.0
                } else 0.0)
                put("snapshots", JSONArray(metrics.memorySnapshots))
            })
            put("network", JSONObject().apply {
                put("totalDataMB", metrics.totalDataDownloaded / 1024.0 / 1024.0)
            })
        }
        
        return json.toString(2)
    }
}
