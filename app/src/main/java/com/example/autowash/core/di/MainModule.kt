package com.example.autowash.core.di

import com.example.autowash.core.settings.AppPreferences
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val mainModule = module {
    single<AppPreferences> {
        AppPreferences(androidApplication())
    }
}