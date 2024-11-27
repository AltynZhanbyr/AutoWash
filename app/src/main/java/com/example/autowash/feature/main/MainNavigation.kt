package com.example.autowash.feature.main

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object Main

fun NavHostController.navigateToMain() = navigate(Main)

fun NavGraphBuilder.mainScreen() {
    composable<Main> {
        MainScreen()
    }
}