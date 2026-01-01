package com.sidhu.androidautoglm.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun CollapsibleExecutionLog(
    logs: List<String>,
    modifier: Modifier = Modifier,
    defaultExpanded: Boolean = true,
    maxVisibleItems: Int = 5
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }
    val visibleLogs = if (expanded) logs else logs.takeLast(maxVisibleItems)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.execution_log_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                    Badge {
                        Text(
                            text = "${logs.size}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (logs.size > maxVisibleItems) {
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) 
                                    stringResource(R.string.collapse) 
                                else 
                                    stringResource(R.string.expand_all),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Log entries with animation
            AnimatedContent(
                targetState = visibleLogs,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "log_animation"
            ) { currentLogs ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentLogs.forEach { logEntry ->
                        ExecutionLogItemEnhanced(
                            logEntry = logEntry,
                            isLatest = logEntry == currentLogs.lastOrNull()
                        )
                    }
                }
            }
            
            // Show "show more" hint
            if (!expanded && logs.size > maxVisibleItems) {
                Text(
                    text = stringResource(R.string.log_show_more, logs.size - maxVisibleItems),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ExecutionLogItemEnhanced(
    logEntry: String,
    isLatest: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isLatest) Color(0xFFE3F2FD) else Color(0xFFFAFAFA)
    val borderColor = if (isLatest) MaterialTheme.colorScheme.primary else Color.Transparent
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        border = if (isLatest) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Indicator dot
            if (isLatest) {
                PulsingDot(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Surface(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(6.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.Gray.copy(alpha = 0.5f)
                ) {}
            }
            
            Text(
                text = logEntry,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isLatest) FontWeight.Medium else FontWeight.Normal
                ),
                color = if (isLatest) Color.Black else Color.DarkGray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun LiveExecutionIndicator(
    currentPhase: String,
    currentAction: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color(0xFFE8F5E9),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PulsingDot(
                modifier = Modifier.size(12.dp),
                color = Color(0xFF4CAF50)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentPhase,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF2E7D32)
                )
                if (currentAction.isNotEmpty()) {
                    Text(
                        text = currentAction,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF558B2F)
                    )
                }
            }
            
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF4CAF50)
            )
        }
    }
}
