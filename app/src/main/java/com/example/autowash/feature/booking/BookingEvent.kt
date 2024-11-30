package com.example.autowash.feature.booking

sealed interface BookingEvent {
    data class ChangeSearch(val value: String) : BookingEvent
    data class GetCurrentPosition(val longitude: String, val latitude: String) : BookingEvent
    data class ChangeBookingSelectedScreen(val value: BookingScreens) : BookingEvent
}