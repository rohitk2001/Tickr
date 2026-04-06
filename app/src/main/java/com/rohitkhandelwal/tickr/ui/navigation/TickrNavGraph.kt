package com.rohitkhandelwal.tickr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rohitkhandelwal.tickr.di.AppContainer
import com.rohitkhandelwal.tickr.ui.screen.taskedit.TaskEditScreen
import com.rohitkhandelwal.tickr.ui.screen.taskedit.TaskEditViewModel
import com.rohitkhandelwal.tickr.ui.screen.taskedit.TaskEditViewModelFactory
import com.rohitkhandelwal.tickr.ui.screen.tasklist.TaskListScreen
import com.rohitkhandelwal.tickr.ui.screen.tasklist.TaskListViewModel
import com.rohitkhandelwal.tickr.ui.screen.tasklist.TaskListViewModelFactory

@Composable
fun TickrNavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TickrDestination.TaskList.route,
        modifier = modifier
    ) {
        composable(TickrDestination.TaskList.route) {
            val viewModel: TaskListViewModel = viewModel(
                factory = TaskListViewModelFactory(appContainer.taskRepository)
            )

            TaskListScreen(
                viewModel = viewModel,
                onAddTask = {
                    navController.navigate(TickrDestination.TaskEdit.createRoute())
                },
                onEditTask = { taskId ->
                    navController.navigate(TickrDestination.TaskEdit.createRoute(taskId))
                }
            )
        }

        composable(
            route = TickrDestination.TaskEdit.route,
            arguments = listOf(
                navArgument(TickrDestination.TaskEdit.TASK_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString(TickrDestination.TaskEdit.TASK_ID_ARG)
            val viewModel: TaskEditViewModel = viewModel(
                factory = TaskEditViewModelFactory(
                    taskRepository = appContainer.taskRepository,
                    taskId = taskId
                )
            )

            TaskEditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
