package com.example.autowash.feature.booking.model

import androidx.compose.runtime.Immutable
import com.yandex.mapkit.GeoObject

@Immutable
data class BookingUIState(
    val searchField: String = "",
    val selectedBookingScreen: BookingScreens = BookingScreens.MainBookingScreen,
    val searchedGeoObjects: List<GeoObject> = emptyList(),
    val userPosition: Location? = null
)

enum class BookingScreens {
    MapScreen,
    MainBookingScreen;

    fun isMapScreen() = this == MapScreen
}