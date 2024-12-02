package com.example.autowash.feature.booking.model

import com.example.autowash.core.model.DaysCalendar
import com.yandex.mapkit.GeoObject

sealed interface BookingEvent {
    data class ChangeSearch(val value: String) : BookingEvent
    data class GetCurrentPosition(val latitude: Double, val longitude: Double) : BookingEvent
    data class ChangeBookingSelectedScreen(val value: BookingScreens) : BookingEvent

    data class SetGeoObjectList(val values: List<GeoObject>) : BookingEvent
    data class SelectedGeoObject(val value: GeoObject, val distValue: Double) : BookingEvent

    data class SelectCityMapDropdown(val value: Int) : BookingEvent

    data class SelectDate(val value: DaysCalendar) : BookingEvent
    data class SelectTime(val value: String) : BookingEvent

    data object ClearDateTimeData : BookingEvent
    data class ChangePhoneNumber(val value: String) : BookingEvent
}