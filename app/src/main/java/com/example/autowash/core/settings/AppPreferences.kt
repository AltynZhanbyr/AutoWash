package com.example.autowash.core.settings

import android.content.Context
import com.example.autowash.feature.booking.model.MapCity

class AppPreferences(private val context: Context) {
    private val preferences = context.getSharedPreferences(APP_REFERENCES, Context.MODE_PRIVATE)
    private val editMode = preferences.edit()

    private companion object {
        const val APP_REFERENCES = "autoWashPreferences"

        const val SELECTED_CITY_MAP = "selectedMapCity"
        const val SELECTED_GEO_OBJECT_NAME = "selectedGeoObjectName"
        const val SELECTED_GEO_OBJECT_LATITUDE = "selectedGeoObjectLat"
        const val SELECTED_GEO_OBJECT_LONGITUDE = "selectedGeoObjectLong"
    }

    fun setCityMap(value: String) {
        editMode.putString(SELECTED_CITY_MAP, value)
        editMode.apply()
    }

    fun getCityMap() =
        preferences.getString(SELECTED_CITY_MAP, MapCity.entries[0].cityId)
            ?: MapCity.entries[0].cityId

    fun setGeoObjectName(value: String) {
        editMode.putString(SELECTED_GEO_OBJECT_NAME, value)
        editMode.apply()
    }

    fun getGeoObjectName() = preferences.getString(SELECTED_GEO_OBJECT_NAME, "") ?: ""

    fun setGeoObjectLatitude(value: Double) {
        editMode.putFloat(SELECTED_GEO_OBJECT_LATITUDE, value.toFloat())
        editMode.apply()
    }

    fun getGeoObjectLatitude() = preferences.getFloat(SELECTED_GEO_OBJECT_LATITUDE, 0.0f)

    fun setGeoObjectLongitude(value: Double) {
        editMode.putFloat(SELECTED_GEO_OBJECT_LONGITUDE, value.toFloat())
        editMode.apply()
    }

    fun getGeoObjectLongitude() = preferences.getFloat(SELECTED_GEO_OBJECT_LONGITUDE, 0.0f)
}
