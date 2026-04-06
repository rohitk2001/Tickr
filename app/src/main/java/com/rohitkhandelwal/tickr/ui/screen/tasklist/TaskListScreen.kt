package com.rohitkhandelwal.tickr.ui.screen.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rohitkhandelwal.tickr.core.time.TaskDateFormatter
import com.rohitkhandelwal.tickr.domain.model.Task
import com.rohitkhandelwal.tickr.domain.model.TaskFilter
import com.rohitkhandelwal.tickr.domain.model.TaskSyncStatus

@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    TaskListScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAddTask = onAddTask,
        onEditTask = onEditTask,
        onFilterSelected = viewModel::onFilterSelected,
        onToggleCompleted = viewModel::onToggleCompleted,
        onDeleteTask = viewModel::onDeleteTask,
        onRetrySync = viewModel::onRetrySync
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListScreen(
    uiState: TaskListUiState,
    snackbarHostState: SnackbarHostState,
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit,
    onFilterSelected: (TaskFilter) -> Unit,
    onToggleCompleted: (String, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit,
    onRetrySync: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tickr")
                        Text(
                            text = "Offline-first task manager",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onRetrySync) {
                        Text(if (uiState.isSyncing) "Syncing..." else "Sync")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                text = { Text("Add Task") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            FilterRow(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = onFilterSelected
            )

            if (uiState.isEmpty) {
                EmptyState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.tasks,
                        key = { it.id }
                    ) { task ->
                        TaskCard(
                            task = task,
                            onClick = { onEditTask(task.id) },
                            onToggleCompleted = { checked ->
                                onToggleCompleted(task.id, checked)
                            },
                            onDeleteTask = { onDeleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selectedFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label()) }
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Create your first task to see local-first updates and sync states.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onToggleCompleted: (Boolean) -> Unit,
    onDeleteTask: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onToggleCompleted
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                    )

                    if (!task.description.isNullOrBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SyncChip(syncStatus = task.syncStatus)

                    val dueDate = TaskDateFormatter.format(task.dueDate)
                    if (dueDate.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Due $dueDate") }
                        )
                    }
                }

                Button(onClick = onDeleteTask) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun SyncChip(syncStatus: TaskSyncStatus) {
    val label = when (syncStatus) {
        TaskSyncStatus.SYNCED -> "Synced"
        TaskSyncStatus.PENDING -> "Pending"
        TaskSyncStatus.SYNCING -> "Syncing"
        TaskSyncStatus.FAILED -> "Failed"
    }

    Card {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun TaskFilter.label(): String {
    return when (this) {
        TaskFilter.ALL -> "All"
        TaskFilter.ACTIVE -> "Active"
        TaskFilter.COMPLETED -> "Completed"
    }
}
