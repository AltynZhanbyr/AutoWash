package com.example.autowash.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Location(
    val latitude: Double,
    val longitude: Double
)