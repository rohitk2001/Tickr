package com.rohitkhandelwal.tickr.ui.screen.tasklist

import com.rohitkhandelwal.tickr.domain.model.Task
import com.rohitkhandelwal.tickr.domain.model.TaskFilter

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val isSyncing: Boolean = false
) {
    val isEmpty: Boolean = tasks.isEmpty()
}
