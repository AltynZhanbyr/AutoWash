package com.example.autowash.ui.theme

import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val onBackground: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tint: Color
)

val lightColors = AppColors(
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    primary = Color(0xFF44B2CC),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF0958B1),
    onSecondary = Color(0xFFFFFFFF),
    tint = Color(0xFFB7B7B7)
)

val darkColors = AppColors(
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    primary = Color(0xFF44B2CC),
    onPrimary = Color(0xFF121212),
    secondary = Color(0xFF0958B1),
    onSecondary = Color(0xFFE0E0E0),
    tint = Color(0xFFB7B7B7)
)
