package com.cpotzy.thedecider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cpotzy.thedecider.ui.queue.QueueScreen
import com.cpotzy.thedecider.ui.queue.QueueViewModel
import com.cpotzy.thedecider.ui.theme.TheDeciderTheme

class MainActivity : ComponentActivity() {
    private val viewModel: QueueViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val graph = (application as App).graph
                @Suppress("UNCHECKED_CAST")
                return QueueViewModel(
                    taskRepository = graph.taskRepository,
                    completionRepository = graph.completionRepository,
                    snoozeRepository = graph.snoozeRepository,
                    selectionService = graph.selectionService,
                    pressureCalc = graph.pressureCalculator,
                    clock = graph.clock,
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheDeciderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    QueueScreen(viewModel = viewModel)
                }
            }
        }
    }
}
