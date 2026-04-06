package com.rohitkhandelwal.tickr.ui.navigation

sealed interface TickrDestination {
    val route: String

    data object TaskList : TickrDestination {
        override val route: String = "task_list"
    }

    data object TaskEdit : TickrDestination {
        override val route: String = "task_edit?taskId={taskId}"
        const val TASK_ID_ARG = "taskId"

        fun createRoute(taskId: String? = null): String {
            return if (taskId.isNullOrBlank()) {
                "task_edit"
            } else {
                "task_edit?taskId=$taskId"
            }
        }
    }
}
