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
import androidx.compose.ui.res.stringResource
import com.sidhu.androidautoglm.R
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun ErrorDetailCard(
    error: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.error_occurred),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFD32F2F)
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Error message (shortened)
            val shortError = if (error.length > 100 && !expanded) {
                error.take(100) + "..."
            } else {
                error
            }
            
            Text(
                text = shortError,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
            
            // Expand/Collapse button if error is long
            if (error.length > 100) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expanded) 
                            stringResource(R.string.show_less) 
                        else 
                            stringResource(R.string.show_more),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Error analysis and suggestions
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = Color(0xFFFFCDD2))
                    
                    Text(
                        text = stringResource(R.string.possible_solutions),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFD32F2F)
                    )
                    
                    // Analyze error and provide suggestions
                    val suggestions = analyzeError(error)
                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy error button
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(error))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.copy_error))
                }
                
                // Retry button (if provided)
                onRetry?.let { retry ->
                    Button(
                        onClick = retry,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

/**
 * 分析错误并提供建议
 */
private fun analyzeError(error: String): List<String> {
    val suggestions = mutableListOf<String>()
    
    when {
        error.contains("API Key", ignoreCase = true) || error.contains("apikey", ignoreCase = true) -> {
            suggestions.add("检查API Key是否正确配置")
            suggestions.add("确认API Key有足够的配额")
            suggestions.add("尝试重新设置API Key")
        }
        error.contains("network", ignoreCase = true) || error.contains("网络", ignoreCase = true) -> {
            suggestions.add("检查网络连接是否正常")
            suggestions.add("尝试切换WiFi或移动数据")
            suggestions.add("检查是否有网络代理或VPN影响")
        }
        error.contains("timeout", ignoreCase = true) || error.contains("超时", ignoreCase = true) -> {
            suggestions.add("网络响应超时，请重试")
            suggestions.add("检查网络速度是否稳定")
            suggestions.add("考虑增加超时时间设置")
        }
        error.contains("screenshot", ignoreCase = true) || error.contains("截图", ignoreCase = true) -> {
            suggestions.add("确认无障碍服务权限已开启")
            suggestions.add("检查悬浮窗权限是否正常")
            suggestions.add("重启应用后再试")
        }
        error.contains("permission", ignoreCase = true) || error.contains("权限", ignoreCase = true) -> {
            suggestions.add("检查所有必需权限是否已授予")
            suggestions.add("进入设置页面重新授权")
            suggestions.add("确认无障碍服务处于开启状态")
        }
        error.contains("memory", ignoreCase = true) || error.contains("内存", ignoreCase = true) -> {
            suggestions.add("当前内存使用过高")
            suggestions.add("尝试清理其他应用释放内存")
            suggestions.add("重启应用清理缓存")
        }
        error.contains("service", ignoreCase = true) || error.contains("服务", ignoreCase = true) -> {
            suggestions.add("无障碍服务可能已断开")
            suggestions.add("尝试关闭并重新开启服务")
            suggestions.add("检查系统设置中的服务状态")
        }
        else -> {
            suggestions.add("请查看详细错误信息")
            suggestions.add("尝试重启应用")
            suggestions.add("如果问题持续，请联系支持")
        }
    }
    
    return suggestions
}

@Composable
fun QuickErrorToast(
    error: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFD32F2F),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = error.take(100),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
