package com.rexmtorres.cmp

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.apply {
            set(applicationContext)
        }
    }
}
