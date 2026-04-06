package com.rohitkhandelwal.tickr.ui.screen.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohitkhandelwal.tickr.domain.model.TaskFilter
import com.rohitkhandelwal.tickr.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskListViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(TaskFilter.ALL)
    private val isSyncing = MutableStateFlow(false)

    val messages = MutableSharedFlow<String>()

    val uiState = combine(
        selectedFilter,
        isSyncing,
        selectedFilter.flatMapLatest { filter ->
            taskRepository.observeTasks(filter)
        }
    ) { filter, syncing, tasks ->
        TaskListUiState(
            tasks = tasks,
            selectedFilter = filter,
            isSyncing = syncing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskListUiState()
    )

    fun onFilterSelected(filter: TaskFilter) {
        selectedFilter.value = filter
    }

    fun onToggleCompleted(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompleted(taskId, isCompleted)
        }
    }

    fun onDeleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            messages.emit("Task deleted locally and queued for sync.")
        }
    }

    fun onRetrySync() {
        viewModelScope.launch {
            isSyncing.update { true }
            val result = taskRepository.syncPendingTasks()
            isSyncing.update { false }

            val message = when {
                result.failedCount > 0 -> {
                    "Synced ${result.syncedCount} tasks, ${result.failedCount} failed."
                }

                result.syncedCount > 0 -> {
                    "Synced ${result.syncedCount} pending tasks."
                }

                else -> {
                    "Nothing pending to sync."
                }
            }
            messages.emit(message)
        }
    }
}
