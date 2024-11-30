package com.example.autowash.feature.booking

data class BookingUIState(
    val searchField: String = "",
    val longitude: String = "",
    val latitude: String = "",
    val selectedBookingScreen: BookingScreens = BookingScreens.MainBookingScreen
)

enum class BookingScreens {
    MapScreen,
    MainBookingScreen;

    fun isMapScreen() = this == MapScreen
}