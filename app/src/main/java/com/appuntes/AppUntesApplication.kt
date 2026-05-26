package com.appuntes

import android.app.Application
import com.appuntes.core.util.NotificationHelper

class AppUntesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.crearCanal(this)
    }
}
