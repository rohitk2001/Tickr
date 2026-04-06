package com.rohitkhandelwal.tickr.ui.screen.taskedit

data class TaskEditUiState(
    val taskId: String? = null,
    val title: String = "",
    val description: String = "",
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null
) {
    val isEditMode: Boolean = taskId != null
}
