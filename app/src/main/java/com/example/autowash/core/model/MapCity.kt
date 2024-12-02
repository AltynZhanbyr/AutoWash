package com.example.autowash.core.model

import com.yandex.mapkit.geometry.Point

enum class MapCity(
    val cityId: String,
    val cityName: String,
    val cityLatLong: Point,
    val southWest: Point,
    val northEast: Point,
) {
    Astana(
        cityId = "ast",
        cityName = "Астана",
        cityLatLong = Point(51.169392, 71.449074),
        southWest = Point(50.965501, 71.054517),
        northEast = Point(51.277534, 71.704083)
    ),

    Almaty(
        cityId = "alm",
        cityName = "Алматы",
        cityLatLong = Point(43.238949, 76.889709),
        southWest = Point(43.121349, 76.676026),
        northEast = Point(43.445841, 77.196262)
    );

    companion object {
        fun toDropdownList(): List<MapCityDropdown> = entries.mapIndexed { idx, city ->
            MapCityDropdown(
                idx = idx,
                title = city.cityName,
                cityId = city.cityId,
                cityLatLong = city.cityLatLong,
                southWest = city.southWest,
                northEast = city.northEast
            )
        }
    }
}