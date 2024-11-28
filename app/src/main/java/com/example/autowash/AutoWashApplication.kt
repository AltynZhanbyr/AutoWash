package com.example.autowash

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class AutoWashApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAP_API)
    }
}