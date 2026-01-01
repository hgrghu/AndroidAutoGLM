package com.sidhu.androidautoglm.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sidhu.androidautoglm.utils.PerformanceMonitor
import androidx.compose.ui.res.stringResource
import com.sidhu.androidautoglm.R

@Composable
fun PerformanceSummaryCard(
    metrics: PerformanceMonitor.TaskMetrics,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    
    val duration = (metrics.endTime ?: System.currentTimeMillis()) - metrics.startTime
    val avgScreenshotTime = if (metrics.screenshotTimings.isNotEmpty()) {
        metrics.screenshotTimings.average()
    } else 0.0
    val avgApiCallTime = if (metrics.apiCallTimings.isNotEmpty()) {
        metrics.apiCallTimings.average()
    } else 0.0
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.performance_summary_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF2E7D32)
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onExport,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.export_metrics),
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Duration
                    MetricRow(
                        icon = Icons.Default.AccessTime,
                        label = stringResource(R.string.metric_duration),
                        value = String.format("%.1fs", duration / 1000.0),
                        color = Color(0xFF1976D2)
                    )
                    
                    // Screenshots
                    MetricRow(
                        icon = Icons.Default.CameraAlt,
                        label = stringResource(R.string.metric_screenshots),
                        value = "${metrics.screenshotCount} (avg: ${String.format("%.0fms", avgScreenshotTime)})",
                        color = Color(0xFFE64A19)
                    )
                    
                    // API Calls
                    MetricRow(
                        icon = Icons.Default.Cloud,
                        label = stringResource(R.string.metric_api_calls),
                        value = "${metrics.apiCallCount} (avg: ${String.format("%.1fs", avgApiCallTime / 1000.0)})",
                        color = Color(0xFF7B1FA2)
                    )
                    
                    // Actions
                    MetricRow(
                        icon = Icons.Default.TouchApp,
                        label = stringResource(R.string.metric_actions),
                        value = "${metrics.actionExecutionCount}",
                        color = Color(0xFFF57C00)
                    )
                    
                    // Memory
                    val memoryMB = metrics.memoryPeakUsage / 1024.0 / 1024.0
                    val memoryColor = when {
                        memoryMB > 500 -> Color(0xFFD32F2F)
                        memoryMB > 300 -> Color(0xFFF57C00)
                        else -> Color(0xFF388E3C)
                    }
                    MetricRow(
                        icon = Icons.Default.Memory,
                        label = stringResource(R.string.metric_memory_peak),
                        value = String.format("%.1f MB", memoryMB),
                        color = memoryColor
                    )
                    
                    // Network Data
                    MetricRow(
                        icon = Icons.Default.CloudDownload,
                        label = stringResource(R.string.metric_data_usage),
                        value = String.format("%.2f MB", metrics.totalDataDownloaded / 1024.0 / 1024.0),
                        color = Color(0xFF0288D1)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

@Composable
fun MemoryWarningBanner(
    currentMemoryMB: Float,
    modifier: Modifier = Modifier
) {
    if (currentMemoryMB > 300) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (currentMemoryMB > 500) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (currentMemoryMB > 500) Color(0xFFD32F2F) else Color(0xFFF57C00),
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentMemoryMB > 500) 
                            stringResource(R.string.memory_warning_high)
                        else 
                            stringResource(R.string.memory_warning_moderate),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (currentMemoryMB > 500) Color(0xFFD32F2F) else Color(0xFFF57C00)
                    )
                    Text(
                        text = String.format("%.1f MB", currentMemoryMB),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
