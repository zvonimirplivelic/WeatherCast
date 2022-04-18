package com.zvonimirplivelic.weathercast

import android.app.Application
import timber.log.Timber

class WeatherCastApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}