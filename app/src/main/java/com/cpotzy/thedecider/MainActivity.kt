package com.cpotzy.thedecider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cpotzy.thedecider.ui.history.HistoryScreen
import com.cpotzy.thedecider.ui.queue.QueueScreen
import com.cpotzy.thedecider.ui.queue.QueueViewModel
import com.cpotzy.thedecider.ui.task.TaskDetailScreen
import com.cpotzy.thedecider.ui.task.TaskDetailViewModel
import com.cpotzy.thedecider.ui.theme.TheDeciderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheDeciderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav(graph = (application as App).graph)
                }
            }
        }
    }
}

@Composable
private fun AppNav(graph: AppGraph) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "queue") {
        composable("queue") {
            val vm: QueueViewModel = viewModel(
                factory = remember {
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return QueueViewModel(
                                taskRepository = graph.taskRepository,
                                completionRepository = graph.completionRepository,
                                snoozeRepository = graph.snoozeRepository,
                                selectionService = graph.selectionService,
                                pressureCalc = graph.pressureCalculator,
                                updateChecker = graph.updateChecker,
                                clock = graph.clock,
                            ) as T
                        }
                    }
                },
            )
            QueueScreen(
                viewModel = vm,
                onAcceptTask = { taskId -> navController.navigate("task/$taskId") },
                onOpenHistory = { navController.navigate("history") },
                taskRepository = graph.taskRepository,
            )
        }
        composable("history") {
            HistoryScreen(
                completionDao = graph.completionDao,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "task/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType }),
        ) { entry ->
            val taskId = entry.arguments?.getLong("taskId") ?: return@composable
            val vm: TaskDetailViewModel = viewModel(
                key = "task-$taskId",
                factory = remember(taskId) {
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return TaskDetailViewModel(
                                taskId = taskId,
                                stepRepository = graph.stepRepository,
                                completionRepository = graph.completionRepository,
                            ) as T
                        }
                    }
                },
            )
            TaskDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onFinished = { navController.popBackStack() },
            )
        }
    }
}
