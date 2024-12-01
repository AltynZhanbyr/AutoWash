package com.example.autowash.feature.booking.model

import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.autowash.R
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.Session.SearchListener
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider

class MapKitListeners(private val context: Context) {
    fun searchListener(
        result: (List<GeoObject>, Boolean) -> Unit
    ): SearchListener = object : SearchListener {
        override fun onSearchResponse(response: Response) {
            val geoObjects = response.collection.children.mapNotNull { item -> item.obj }
            result.invoke(geoObjects, false)
        }

        override fun onSearchError(p0: com.yandex.runtime.Error) {
            result.invoke(emptyList(), true)
        }
    }

    fun userObjectLocationListener(
        strokeColor: Color,
        strokeWidth: Float,
        fillColor: Color
    ): UserLocationObjectListener = object : UserLocationObjectListener {
        override fun onObjectAdded(p0: UserLocationView) {
            p0.arrow.setIcon(
                ImageProvider.fromResource(context, R.drawable.img_gps_location),
                IconStyle().apply {
                    anchor = PointF(0.5f, 1.0f)
                    scale = 0.05f
                    zIndex = 10f
                })

            p0.accuracyCircle.fillColor = fillColor.toArgb()
            p0.accuracyCircle.strokeWidth = strokeWidth
            p0.accuracyCircle.strokeColor = strokeColor.toArgb()
        }

        override fun onObjectRemoved(p0: UserLocationView) {
        }

        override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
            p0.accuracyCircle.fillColor = fillColor.toArgb()
            p0.accuracyCircle.strokeWidth = strokeWidth
            p0.accuracyCircle.strokeColor = strokeColor.toArgb()
        }

    }

    fun geoObjectTapListener(): GeoObjectTapListener = GeoObjectTapListener { p0 ->
        if (p0.isValid && p0.geoObject.name != null) {
            Log.d("ObjectName", p0.geoObject.name!!)
            Toast.makeText(context, p0.geoObject.name!!, Toast.LENGTH_SHORT).show()
        }
        true
    }

    fun cameraListener(
        resultOnFinish: () -> Unit
    ): CameraListener = CameraListener { _, _, _, finished ->
        if (finished) {
            resultOnFinish.invoke()
        }
    }

}