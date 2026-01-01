package com.sidhu.androidautoglm.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.res.stringResource
import com.sidhu.androidautoglm.R
import com.sidhu.androidautoglm.utils.PerformanceMonitor

data class PerformanceRecommendation(
    val title: String,
    val description: String,
    val severity: Severity,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    enum class Severity {
        INFO, WARNING, CRITICAL
    }
}

/**
 * 分析性能数据并生成建议
 */
fun analyzePerformance(metrics: PerformanceMonitor.TaskMetrics): List<PerformanceRecommendation> {
    val recommendations = mutableListOf<PerformanceRecommendation>()
    
    val duration = (metrics.endTime ?: System.currentTimeMillis()) - metrics.startTime
    val avgScreenshotTime = if (metrics.screenshotTimings.isNotEmpty()) {
        metrics.screenshotTimings.average()
    } else 0.0
    val avgApiCallTime = if (metrics.apiCallTimings.isNotEmpty()) {
        metrics.apiCallTimings.average()
    } else 0.0
    val memoryPeakMB = metrics.memoryPeakUsage / 1024.0 / 1024.0
    
    // 1. API调用时间分析
    if (avgApiCallTime > 5000) {
        recommendations.add(
            PerformanceRecommendation(
                title = "API响应较慢",
                description = "平均API响应时间 ${String.format("%.1fs", avgApiCallTime / 1000.0)}，建议检查网络连接或考虑使用更快的模型。",
                severity = PerformanceRecommendation.Severity.WARNING,
                icon = Icons.Default.CloudOff
            )
        )
    } else if (avgApiCallTime < 2000) {
        recommendations.add(
            PerformanceRecommendation(
                title = "API性能优秀",
                description = "平均响应时间 ${String.format("%.1fs", avgApiCallTime / 1000.0)}，网络状况良好。",
                severity = PerformanceRecommendation.Severity.INFO,
                icon = Icons.Default.CheckCircle
            )
        )
    }
    
    // 2. 截图性能分析
    if (avgScreenshotTime > 500) {
        recommendations.add(
            PerformanceRecommendation(
                title = "截图耗时较长",
                description = "平均截图时间 ${String.format("%.0fms", avgScreenshotTime)}，可能影响整体速度。建议启用截图压缩。",
                severity = PerformanceRecommendation.Severity.WARNING,
                icon = Icons.Default.Camera
            )
        )
    }
    
    // 3. 内存使用分析
    when {
        memoryPeakMB > 500 -> {
            recommendations.add(
                PerformanceRecommendation(
                    title = "内存占用过高",
                    description = "峰值内存 ${String.format("%.1fMB", memoryPeakMB)}，建议清理消息历史或重启应用。",
                    severity = PerformanceRecommendation.Severity.CRITICAL,
                    icon = Icons.Default.Warning
                )
            )
        }
        memoryPeakMB > 300 -> {
            recommendations.add(
                PerformanceRecommendation(
                    title = "内存使用偏高",
                    description = "峰值内存 ${String.format("%.1fMB", memoryPeakMB)}，注意定期清理以保持性能。",
                    severity = PerformanceRecommendation.Severity.WARNING,
                    icon = Icons.Default.Memory
                )
            )
        }
        else -> {
            recommendations.add(
                PerformanceRecommendation(
                    title = "内存使用正常",
                    description = "峰值内存 ${String.format("%.1fMB", memoryPeakMB)}，在合理范围内。",
                    severity = PerformanceRecommendation.Severity.INFO,
                    icon = Icons.Default.CheckCircle
                )
            )
        }
    }
    
    // 4. 任务效率分析
    val stepsPerMinute = (metrics.actionExecutionCount.toFloat() / (duration / 60000f))
    if (stepsPerMinute < 2) {
        recommendations.add(
            PerformanceRecommendation(
                title = "执行效率偏低",
                description = "每分钟执行 ${String.format("%.1f", stepsPerMinute)} 个动作，可能需要优化任务或检查模型响应。",
                severity = PerformanceRecommendation.Severity.WARNING,
                icon = Icons.Default.Speed
            )
        )
    }
    
    // 5. 数据使用分析
    val dataMB = metrics.totalDataDownloaded / 1024.0 / 1024.0
    if (dataMB > 50) {
        recommendations.add(
            PerformanceRecommendation(
                title = "数据用量较大",
                description = "本次任务使用 ${String.format("%.1fMB", dataMB)} 流量，建议在WiFi环境下使用。",
                severity = PerformanceRecommendation.Severity.WARNING,
                icon = Icons.Default.DataUsage
            )
        )
    }
    
    // 6. 总体评分
    val overallScore = calculateOverallScore(metrics)
    if (overallScore >= 80) {
        recommendations.add(
            PerformanceRecommendation(
                title = "性能表现优秀",
                description = "综合评分 $overallScore/100，各项指标均表现良好。",
                severity = PerformanceRecommendation.Severity.INFO,
                icon = Icons.Default.Star
            )
        )
    }
    
    return recommendations
}

/**
 * 计算综合性能评分 (0-100)
 */
private fun calculateOverallScore(metrics: PerformanceMonitor.TaskMetrics): Int {
    var score = 100
    
    val avgApiCallTime = if (metrics.apiCallTimings.isNotEmpty()) {
        metrics.apiCallTimings.average()
    } else 0.0
    val avgScreenshotTime = if (metrics.screenshotTimings.isNotEmpty()) {
        metrics.screenshotTimings.average()
    } else 0.0
    val memoryPeakMB = metrics.memoryPeakUsage / 1024.0 / 1024.0
    
    // API响应时间扣分
    if (avgApiCallTime > 5000) score -= 20
    else if (avgApiCallTime > 3000) score -= 10
    
    // 截图时间扣分
    if (avgScreenshotTime > 500) score -= 10
    else if (avgScreenshotTime > 300) score -= 5
    
    // 内存使用扣分
    if (memoryPeakMB > 500) score -= 30
    else if (memoryPeakMB > 300) score -= 15
    
    return score.coerceIn(0, 100)
}

@Composable
fun PerformanceRecommendationCard(
    metrics: PerformanceMonitor.TaskMetrics,
    modifier: Modifier = Modifier
) {
    val recommendations = remember(metrics) { analyzePerformance(metrics) }
    var expanded by remember { mutableStateOf(true) }
    
    if (recommendations.isEmpty()) return
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
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
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.performance_recommendations),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                }
                
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
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
                    recommendations.forEach { recommendation ->
                        RecommendationItem(recommendation)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    recommendation: PerformanceRecommendation,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (recommendation.severity) {
        PerformanceRecommendation.Severity.CRITICAL -> Color(0xFFFFEBEE)
        PerformanceRecommendation.Severity.WARNING -> Color(0xFFFFF3E0)
        PerformanceRecommendation.Severity.INFO -> Color(0xFFE8F5E9)
    }
    
    val iconColor = when (recommendation.severity) {
        PerformanceRecommendation.Severity.CRITICAL -> Color(0xFFD32F2F)
        PerformanceRecommendation.Severity.WARNING -> Color(0xFFF57C00)
        PerformanceRecommendation.Severity.INFO -> Color(0xFF388E3C)
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = recommendation.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}
