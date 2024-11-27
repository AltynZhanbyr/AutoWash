package com.example.autowash.util

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import com.example.autowash.ui.theme.AppColors

val LocalColors = compositionLocalOf<AppColors> { error("App colors do not provided") }
val LocalNavHost = compositionLocalOf<NavHostController> { error("NavController is not provided") }