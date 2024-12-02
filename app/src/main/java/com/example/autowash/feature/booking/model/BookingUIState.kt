package com.example.autowash.feature.booking.model

import androidx.compose.runtime.Immutable
import com.example.autowash.core.model.AppGeoObject
import com.example.autowash.core.model.DaysCalendar
import com.example.autowash.core.model.Location
import com.example.autowash.core.model.MapCity
import com.example.autowash.core.model.MapCityDropdown
import com.example.autowash.util.getCalendar
import com.yandex.mapkit.GeoObject

@Immutable
data class BookingUIState(
    val searchField: String = "",
    val selectedBookingScreen: BookingScreens = BookingScreens.MainBookingScreen,
    val searchedGeoObjects: List<GeoObject> = emptyList(),
    val userPosition: Location? = null,
    val cityMapList: List<MapCityDropdown> = MapCity.toDropdownList(),
    val selectedMapDropdown: MapCityDropdown? = null,
    val selectedGeoObject: AppGeoObject? = null,

    val daysCalendar: List<DaysCalendar> = getCalendar(10),
    val selectedDay: DaysCalendar? = null,
    val times: List<String> = (10..21).map { it.toString() },
    val selectedTime: String? = null,
    val phoneNumber: String = ""
)

enum class BookingScreens {
    MapScreen,
    ScheduleScreen,
    MainBookingScreen;

    fun isMapScreen() = this == MapScreen
    fun isScheduleScreen() = this == ScheduleScreen
}