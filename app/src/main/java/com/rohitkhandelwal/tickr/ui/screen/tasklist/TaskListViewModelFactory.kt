package com.rohitkhandelwal.tickr.ui.screen.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rohitkhandelwal.tickr.domain.repository.TaskRepository

class TaskListViewModelFactory(
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskListViewModel(taskRepository) as T
    }
}
