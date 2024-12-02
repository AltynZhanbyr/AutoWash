package com.example.autowash.core.model

import com.yandex.mapkit.geometry.Point

abstract class Dropdown(
    open val idx: Int,
    open val title: String
)

class BasicDropdown(
    override val idx: Int,
    override val title: String
) : Dropdown(idx, title)

class MapCityDropdown(
    override val idx: Int,
    override val title: String,
    val cityId: String,
    val cityLatLong: Point,
    val southWest: Point,
    val northEast: Point,
) : Dropdown(idx, title)

