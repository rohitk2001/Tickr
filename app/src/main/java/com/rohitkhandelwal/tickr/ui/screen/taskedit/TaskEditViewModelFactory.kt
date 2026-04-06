package com.rohitkhandelwal.tickr.ui.screen.taskedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rohitkhandelwal.tickr.domain.repository.TaskRepository

class TaskEditViewModelFactory(
    private val taskRepository: TaskRepository,
    private val taskId: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskEditViewModel(
            taskRepository = taskRepository,
            taskId = taskId
        ) as T
    }
}
