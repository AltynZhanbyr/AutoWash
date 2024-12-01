package com.example.autowash.feature.booking

import androidx.lifecycle.ViewModel
import com.example.autowash.core.settings.AppPreferences
import com.example.autowash.feature.booking.model.AppGeoObject
import com.example.autowash.feature.booking.model.BookingEvent
import com.example.autowash.feature.booking.model.BookingUIState
import com.example.autowash.feature.booking.model.Location
import com.example.autowash.feature.booking.model.MapCity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BookingViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val _state = MutableStateFlow(BookingUIState())
    val state = _state.asStateFlow()

    init {
        val selectedMap = MapCity.toDropdownList().find { city ->
            city.cityId == appPreferences.getCityMap()
        }

        val selectedGeoObjectName = appPreferences.getGeoObjectName()
        val selectedGeoObjectLatitude = appPreferences.getGeoObjectLatitude()
        val selectedGeoObjectLongitude = appPreferences.getGeoObjectLongitude()

        val appGeoObject =
            if (selectedGeoObjectName.isNotBlank() && selectedGeoObjectLongitude > 0.0f && selectedGeoObjectLatitude > 0.0f)
                AppGeoObject(
                    selectedGeoObjectName,
                    selectedGeoObjectLatitude,
                    selectedGeoObjectLongitude
                )
            else null

        _state.update { state ->
            state.copy(
                selectedMapDropdown = selectedMap,
                selectedGeoObject = appGeoObject
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
}