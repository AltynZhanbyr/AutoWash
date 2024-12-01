package com.example.autowash.core.di

import com.example.autowash.feature.booking.BookingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureModule = module {
    viewModelOf(::BookingViewModel)
}