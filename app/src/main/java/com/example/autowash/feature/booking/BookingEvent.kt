package com.example.autowash.feature.booking

sealed interface BookingEvent {
    data class ChangeSearch(val value: String) : BookingEvent
}