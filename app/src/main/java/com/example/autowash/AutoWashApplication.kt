package com.example.autowash

import android.app.Application
import com.example.autowash.core.di.featureModule
import com.example.autowash.core.di.mainModule
import com.yandex.mapkit.MapKitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AutoWashApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@AutoWashApplication)
            modules(
                mainModule,
                featureModule
            )
        }

        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAP_API)
        MapKitFactory.initialize(this)
    }
}