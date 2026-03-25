package io.nekohasekai.sfa.compose.screen.dashboard.groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.nekohasekai.libbox.Libbox
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
                androidx.compose.material3.Text(
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
                androidx.compose.material3.Text(
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
                    androidx.compose.material3.Text(
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
