package io.nekohasekai.sfa.compose.screen.dashboard.groups

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.compose.model.Group
import io.nekohasekai.sfa.compose.model.GroupItem
import io.nekohasekai.sfa.compose.theme.LatencyYellow
import io.nekohasekai.sfa.compose.theme.SuccessGreen
import io.nekohasekai.sfa.compose.theme.WarningOrange

fun latencyColor(delay: Int, urlTestTime: Long): Color {
    if (urlTestTime == 0L) return Color.Gray
    return when {
        delay < 800 -> SuccessGreen
        delay < 1500 -> LatencyYellow
        else -> WarningOrange
    }
}

// iOS-style group card: flat header + collapsed dot heatmap / expanded card grid
@Composable
fun ProxyGroupCard(
    group: Group,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onItemSelected: (String) -> Unit,
    onUrlTest: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // iOS-style flat header
        ProxyGroupHeader(
            group = group,
            isExpanded = isExpanded,
            onToggleExpanded = onToggleExpanded,
            onUrlTest = onUrlTest,
        )

        // Collapsed: dot heatmap, Expanded: card grid
        if (group.items.isNotEmpty()) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "GroupContent",
            ) { expanded ->
                if (expanded) {
                    ProxyItemsGrid(
                        items = group.items,
                        selectedTag = group.selected,
                        isSelectable = group.selectable,
                        onItemSelected = onItemSelected,
                    )
                } else {
                    ProxyDotHeatmap(
                        items = group.items,
                        selectedTag = group.selected,
                    )
                }
            }
        }
    }
}

// iOS-style flat header: tag + type + count badge + expand arrow + url test bolt
@Composable
private fun ProxyGroupHeader(
    group: Group,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onUrlTest: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpanded)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Group tag name
        Text(
            text = group.tag,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Display type
        Text(
            text = Libbox.proxyDisplayType(group.type),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Item count badge
        Text(
            text = "${group.items.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .background(
                    color = Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp),
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )

        // Spacer to push icons to the right
        Box(modifier = Modifier.weight(1f))

        // Expand/collapse arrow
        IconButton(
            onClick = onToggleExpanded,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = if (isExpanded) {
                    Icons.Default.KeyboardArrowDown
                } else {
                    Icons.Default.KeyboardArrowUp
                },
                contentDescription = if (isExpanded) {
                    stringResource(R.string.collapse)
                } else {
                    stringResource(R.string.expand)
                },
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // URL test button (only for selectable groups)
        if (group.selectable) {
            IconButton(
                onClick = onUrlTest,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = stringResource(R.string.url_test),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// iOS-style collapsed dot heatmap: colored 10x10 dots showing latency at a glance
@Composable
fun ProxyDotHeatmap(
    items: List<GroupItem>,
    selectedTag: String,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        val dotSize = 10.dp
        val spacing = 5.dp
        val cellSize = dotSize + spacing
        val itemsPerRow = maxOf(1, (maxWidth / cellSize).toInt())
        val chunkedItems = remember(items, itemsPerRow) {
            items.chunked(itemsPerRow)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            chunkedItems.forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    rowItems.forEach { item ->
                        key(item.tag) {
                            Box(
                                modifier = Modifier
                                    .size(dotSize)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(latencyColor(item.urlTestDelay, item.urlTestTime)),
                                contentAlignment = Alignment.Center,
                            ) {
                                // Selected indicator: small white dot in center
                                if (item.tag == selectedTag) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(Color.White, RoundedCornerShape(1.dp)),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProxyItemsGrid(
    items: List<GroupItem>,
    selectedTag: String,
    isSelectable: Boolean,
    onItemSelected: (String) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        val columnMinWidth = 180.dp
        val itemsPerRow = maxOf(2, (maxWidth / columnMinWidth).toInt())
        val chunkedItems = remember(items, itemsPerRow) {
            items.chunked(itemsPerRow)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            chunkedItems.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { item ->
                        key(item.tag) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProxyCard(
                                    item = item,
                                    isSelected = item.tag == selectedTag,
                                    isSelectable = isSelectable,
                                    onClick = { onItemSelected(item.tag) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                    repeat(itemsPerRow - rowItems.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ProxyCard(
    item: GroupItem,
    isSelected: Boolean,
    isSelectable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val surfaceShape = RoundedCornerShape(16.dp)
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLow

    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Top row: Name + Checkmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.tag,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // Bottom row: Type + Latency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = Libbox.proxyDisplayType(item.type),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AnimatedVisibility(
                    visible = item.urlTestTime > 0,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    val color = latencyColor(item.urlTestDelay, item.urlTestTime)
                    Text(
                        text = "${item.urlTestDelay}ms",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = color,
                    )
                }
            }
        }
    }

    if (isSelectable) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = surfaceShape,
            color = surfaceColor,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = surfaceShape,
            color = surfaceColor,
            content = content,
        )
    }
}
