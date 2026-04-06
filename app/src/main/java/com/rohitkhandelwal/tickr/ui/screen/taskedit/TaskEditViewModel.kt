package com.rohitkhandelwal.tickr.ui.screen.taskedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohitkhandelwal.tickr.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskEditViewModel(
    private val taskRepository: TaskRepository,
    private val taskId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskEditUiState(isLoading = taskId != null))
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskEditEvent>()
    val events = _events.asSharedFlow()

    init {
        if (taskId != null) {
            loadTask(taskId)
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update {
            it.copy(
                title = value,
                titleError = null
            )
        }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onDueDateChanged(value: Long?) {
        _uiState.update { it.copy(dueDate = value) }
    }

    fun onCompletedChanged(value: Boolean) {
        _uiState.update { it.copy(isCompleted = value) }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        val title = state.title.trim()

        if (title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            if (state.taskId == null) {
                taskRepository.createTask(
                    title = title,
                    description = state.description.trim().ifBlank { null },
                    dueDate = state.dueDate
                )
            } else {
                taskRepository.updateTask(
                    taskId = state.taskId,
                    title = title,
                    description = state.description.trim().ifBlank { null },
                    dueDate = state.dueDate,
                    isCompleted = state.isCompleted
                )
            }

            _uiState.update { it.copy(isSaving = false) }
            _events.emit(TaskEditEvent.Saved)
        }
    }

    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTask(taskId)
            _uiState.update {
                if (task == null) {
                    it.copy(isLoading = false)
                } else {
                    it.copy(
                        taskId = task.id,
                        title = task.title,
                        description = task.description.orEmpty(),
                        dueDate = task.dueDate,
                        isCompleted = task.isCompleted,
                        isLoading = false
                    )
                }
            }
        }
    }
}

sealed interface TaskEditEvent {
    data object Saved : TaskEditEvent
}
