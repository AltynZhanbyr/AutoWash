package com.example.autowash.feature.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autowash.core.settings.AppPreferences
import com.example.autowash.feature.booking.model.AppGeoObject
import com.example.autowash.feature.booking.model.BookingEvent
import com.example.autowash.feature.booking.model.BookingUIState
import com.example.autowash.feature.booking.model.Location
import com.example.autowash.feature.booking.model.MapCity
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookingViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val _state = MutableStateFlow(BookingUIState())
    val state = _state.asStateFlow()

    init {
        val selectedMap = MapCity.toDropdownList().find { city ->
            city.cityId == appPreferences.getCityMap()
        }

        _state.update { state ->
            state.copy(
                selectedMapDropdown = selectedMap
            )
        }
    }

    fun eventHandler(event: BookingEvent) {
        when (event) {
            is BookingEvent.ChangeSearch -> event.changeSearch()
            is BookingEvent.GetCurrentPosition -> event.getCurrentPosition()
            is BookingEvent.ChangeBookingSelectedScreen -> event.changeBookingSelectedScreen()
            is BookingEvent.SetGeoObjectList -> event.setGeoObjectList()
            is BookingEvent.SelectCityMapDropdown -> event.selectCityMapDropdown()
            is BookingEvent.SelectedGeoObject -> event.selectGeoObject()
        }
    }

    private fun BookingEvent.ChangeSearch.changeSearch() {
        _state.update { state ->
            state.copy(
                searchField = value
            )
        }
    }

    private fun BookingEvent.GetCurrentPosition.getCurrentPosition() {
        _state.update { state ->
            state.copy(
                userPosition = Location(latitude, longitude)
            )
        }
    }

    private fun BookingEvent.ChangeBookingSelectedScreen.changeBookingSelectedScreen() {
        _state.update { state ->
            state.copy(
                selectedBookingScreen = value
            )
        }
    }

    private fun BookingEvent.SetGeoObjectList.setGeoObjectList() {
        _state.update { state ->
            state.copy(
                searchedGeoObjects = values
            )
        }
    }

    private fun BookingEvent.SelectCityMapDropdown.selectCityMapDropdown() {
        _state.update { state ->
            val selectedDropdown =
                state.cityMapList.find { it.idx == value } ?: state.cityMapList[0]

            appPreferences.setCityMap(selectedDropdown.cityId)

            state.copy(
                selectedMapDropdown = selectedDropdown
            )
        }
    }

    private fun BookingEvent.SelectedGeoObject.selectGeoObject() {
        viewModelScope.launch {
            _state.update { state ->
                val geometry = value.geometry[0].point ?: Point(0.0, 0.0)
                if (geometry.latitude > 0.0 && geometry.longitude > 0.0 && value.name != null) {
                    appPreferences.setGeoObjectName(value.name!!)
                    appPreferences.setGeoObjectLatitude(geometry.latitude)
                    appPreferences.setGeoObjectLongitude(geometry.longitude)
                } else return@launch

                state.copy(
                    selectedGeoObject = AppGeoObject(
                        value.name!!,
                        distValue,
                        geometry.latitude.toFloat(),
                        geometry.longitude.toFloat()
                    )
                )
            }
        }
    }
}