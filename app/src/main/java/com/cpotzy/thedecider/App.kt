package com.cpotzy.thedecider

import android.app.Application
import com.cpotzy.thedecider.data.db.AppDatabase
import com.cpotzy.thedecider.data.repo.CompletionRepository
import com.cpotzy.thedecider.data.repo.SnoozeRepository
import com.cpotzy.thedecider.data.repo.StepRepository
import com.cpotzy.thedecider.data.repo.TaskRepository
import com.cpotzy.thedecider.data.seed.TaskSeeder
import com.cpotzy.thedecider.data.update.UpdateChecker
import com.cpotzy.thedecider.work.NudgeWorker
import com.cpotzy.thedecider.work.Notifications
import com.cpotzy.thedecider.domain.select.ContextFilter
import com.cpotzy.thedecider.domain.select.PressureCalculator
import com.cpotzy.thedecider.domain.select.SelectionService
import com.cpotzy.thedecider.domain.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
        graph.scope.launch {
            TaskSeeder.seedIfEmpty(this@App, graph.taskRepository, graph.stepDaoForSeeding, graph.clock)
        }
        Notifications.ensureChannel(this)
        NudgeWorker.schedule(this)
    }
}

class AppGraph(app: Application) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val clock: Clock = Clock.System
    private val db = AppDatabase.build(app)
    val taskRepository = TaskRepository(db.taskDao(), db.completionDao(), clock)
    val completionRepository = CompletionRepository(db.completionDao(), db.taskDao(), clock)
    val snoozeRepository = SnoozeRepository(db.snoozeDao(), clock)
    val stepRepository = StepRepository(db.stepDao(), db.taskDao())
    val stepDaoForSeeding = db.stepDao()
    val completionDao = db.completionDao()

    val pressureCalculator = PressureCalculator()
    val contextFilter = ContextFilter()
    val selectionService = SelectionService(pressureCalculator, contextFilter)

    val updateChecker = UpdateChecker(
        owner = BuildConfig.GITHUB_OWNER,
        repo = BuildConfig.GITHUB_REPO,
        currentSha = BuildConfig.GIT_SHA,
        token = BuildConfig.UPDATE_TOKEN,
    )
}
