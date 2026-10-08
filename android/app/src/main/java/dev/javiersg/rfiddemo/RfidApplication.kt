package dev.javiersg.rfiddemo

import android.app.Application
import dev.javiersg.rfiddemo.di.AppContainer

class RfidApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}