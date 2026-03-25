package io.nekohasekai.sfa.compose.screen.dashboard.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.constant.Status

@Composable
fun GroupsScreen(
    serviceStatus: Status,
    viewModel: GroupsViewModel = viewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onToggleAllGroups: () -> Unit = { viewModel.toggleAllGroups() },
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Stable callbacks to prevent recomposition
    val onToggleExpanded =
        remember<(String) -> Unit> {
            { groupTag -> viewModel.toggleGroupExpand(groupTag) }
        }
    val onItemSelected =
        remember<(String, String) -> Unit> {
            { groupTag, itemTag -> viewModel.selectGroupItem(groupTag, itemTag) }
        }
    val onUrlTest =
        remember<(String) -> Unit> {
            { groupTag -> viewModel.urlTest(groupTag) }
        }

    LaunchedEffect(serviceStatus, viewModel) {
        viewModel.updateServiceStatus(serviceStatus)
    }

    // Show snackbar when needed
    LaunchedEffect(uiState.showCloseConnectionsSnackbar) {
        if (uiState.showCloseConnectionsSnackbar) {
            val message = context.getString(R.string.close_connections_confirm)
            val actionLabel = context.getString(R.string.close)
            val result =
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    duration = androidx.compose.material3.SnackbarDuration.Indefinite,
                    withDismissAction = true,
                )
            when (result) {
                androidx.compose.material3.SnackbarResult.ActionPerformed -> {
                    viewModel.closeConnections()
                }
                androidx.compose.material3.SnackbarResult.Dismissed -> {
                    viewModel.dismissCloseConnectionsSnackbar()
                }
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = uiState.groups,
                key = { it.tag },
                contentType = { "GroupCard" },
            ) { group ->
                ProxyGroupCard(
                    group = group,
                    isExpanded = uiState.expandedGroups.contains(group.tag),
                    onToggleExpanded = { onToggleExpanded(group.tag) },
                    onItemSelected = { itemTag -> onItemSelected(group.tag, itemTag) },
                    onUrlTest = { onUrlTest(group.tag) },
                )
            }
        }
    }
}
