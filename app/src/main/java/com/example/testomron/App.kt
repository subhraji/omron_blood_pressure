package com.example.testomron

import android.app.Application
import android.content.Context

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object{
        lateinit var application: Application
        fun getInstance(): Application{
            return application
        }
    }
}