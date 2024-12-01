package com.example.autowash.feature.booking

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.autowash.feature.booking.model.BookingEvent
import com.example.autowash.feature.booking.model.BookingUIState
import com.example.autowash.feature.booking.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BookingViewModel : ViewModel() {
    private val _state = MutableStateFlow(BookingUIState())
    val state = _state.asStateFlow()

    val searchState = mutableStateOf("")

    fun eventHandler(event: BookingEvent) {
        when (event) {
            is BookingEvent.ChangeSearch -> event.changeSearch()
            is BookingEvent.GetCurrentPosition -> event.getCurrentPosition()
            is BookingEvent.ChangeBookingSelectedScreen -> event.changeBookingSelectedScreen()
            is BookingEvent.SetGeoObjectList -> event.setGeoObjectList()
        }
    }

    private fun BookingEvent.ChangeSearch.changeSearch() {
        searchState.value = value
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
}