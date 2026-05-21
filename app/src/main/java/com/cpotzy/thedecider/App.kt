package com.cpotzy.thedecider

import android.app.Application

class App : Application() {
    lateinit var graph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(this)
    }
}

class AppGraph(private val app: Application) {
    // Empty for now — entries added in later tasks
}
