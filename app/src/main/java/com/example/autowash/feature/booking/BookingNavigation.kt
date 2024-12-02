package com.example.autowash.feature.booking

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object Booking

fun NavGraphBuilder.bookingScreen() {
    composable<Booking> {
        BookingScreen()
    }
}

fun NavHostController.navigateToBookingScreen() = navigate(Booking)