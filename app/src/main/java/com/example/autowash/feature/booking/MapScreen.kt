package com.example.autowash.feature.booking

import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.PointF
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.viewpager.widget.ViewPager.LayoutParams
import com.example.autowash.R
import com.example.autowash.feature.booking.model.BookingEvent
import com.example.autowash.feature.booking.model.BookingUIState
import com.example.autowash.feature.booking.model.MapKitListeners
import com.example.autowash.ui.component.BasicButton
import com.example.autowash.ui.component.TextField
import com.example.autowash.util.LocalColors
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private var mapView: MapView? = null
private var mapKit: MapKit? = null
private var searchSession: Session? = null
private var placemark: PlacemarkMapObject? = null
private var userLocationLayer: UserLocationLayer? = null
private var map: Map? = null

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    state: BookingUIState,
    searchField: String,
    paddingValues: PaddingValues,
    event: (BookingEvent) -> Unit,
    onBackPress: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = LocalColors.current

    var dialogVisibility by remember { mutableStateOf(false) }

    val searchOptions = SearchOptions().apply {
        searchTypes = SearchType.BIZ.value
        resultPageSize = 32
    }

    val locationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {}
    )

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)

    val clientSettings = LocationServices.getSettingsClient(context)
    val gpsTask = clientSettings.checkLocationSettings(builder.build())

    val checkLocationPermissions = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) {
        if (!checkLocationPermissions) {
            locationPermission.launch(permissionList.toTypedArray())
        }
    }

    var lastReqState by remember { mutableStateOf("") }

    val mapKitListeners = MapKitListeners(context)

    val searchSessionListener = mapKitListeners.searchListener(
        result = { geoObjects, isError ->
            dialogVisibility = isError
            event(BookingEvent.SetGeoObjectList(geoObjects))
        }
    )

    val userObjectLocationListener = mapKitListeners.userObjectLocationListener(
        strokeColor = colors.primary,
        strokeWidth = 2f,
        fillColor = colors.primary.copy(alpha = 0.3f)
    )

    val geoObjectTapListener = mapKitListeners.geoObjectTapListener()

    val cameraListener = mapKitListeners.cameraListener {
        searchSession?.cancel()
        map?.let { mapValue ->
            if (searchField.isBlank()) {
                event(BookingEvent.SetGeoObjectList(emptyList()))
                return@let
            }

            searchSession = searchManager.submit(
                searchField,
                VisibleRegionUtils.toPolygon(mapValue.visibleRegion),
                searchOptions,
                searchSessionListener,
            )
        }
    }

    MapScreenLifecycleObserver(
        lifecycleOwner = lifecycleOwner,
        onStart = {
            if (mapKit == null) mapKit = MapKitFactory.getInstance()

            mapKit?.onStart()
            mapView?.onStart()
            mapView?.let { view ->
                if (userLocationLayer == null) userLocationLayer =
                    mapKit?.createUserLocationLayer(view.mapWindow)

                userLocationLayer?.apply {
                    if (isValid) {
                        isVisible = true
                        setObjectListener(userObjectLocationListener)
                    }
                }
            }
        }, onStop = {
            mapKit?.onStop()
            mapView?.onStop()
        })

    LaunchedEffect(state.searchedGeoObjects) {
        withContext(Dispatchers.Main) {
            map?.mapObjects?.clear()

            state.searchedGeoObjects.forEach { geoObject ->
                placemark = map?.mapObjects?.addPlacemark()?.apply {
                    geometry = geoObject.geometry[0].point ?: Point(0.0, 0.0)
                    setIcon(
                        ImageProvider.fromResource(context, R.drawable.img_map_point),
                        IconStyle().apply {
                            anchor = PointF(0.5f, 1.0f)
                            scale = 0.08f
                            zIndex = 10f
                        }
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = { context ->
                mapView = MapView(context)
                map = mapView!!.mapWindow.map

                map?.cameraBounds?.setMinZoomPreference(9f)
                map?.cameraBounds?.latLngBounds = BoundingBox(
                    Point(51.071760, 71.060010),
                    Point(51.249176, 71.734296)
                )

                map?.move(
                    CameraPosition(
                        Point(51.169392, 71.449074),
                        9f,
                        0f,
                        0f
                    )
                )

                map?.addCameraListener(cameraListener)
                mapView!!
            },
            update = { view ->

                map?.addTapListener(geoObjectTapListener)
                view.layoutParams.height = LayoutParams.MATCH_PARENT
                view.layoutParams.width = LayoutParams.MATCH_PARENT
            }
        )

        Row(
            modifier = Modifier
                .padding(
                    start = 9.dp,
                    end = 9.dp,
                    top = 9.dp
                )
                .height(56.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            BasicButton(
                modifier = Modifier
                    .weight(0.1f)
                    .height(56.dp),
                onClick = onBackPress,
                contentColor = colors.onPrimary,
                containerColor = colors.primary,
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                },
                paddingValues = PaddingValues()
            )

            TextField(
                modifier = Modifier
                    .weight(0.9f),
                value = searchField,
                onValueChange = { value ->
                    event(BookingEvent.ChangeSearch(value))
                    lastReqState = value
                },
                placeholder = stringResource(R.string.lbl_search),
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searchSession?.cancel()
                        map?.let { mapValue ->
                            if (searchField.isBlank()) {
                                event(BookingEvent.SetGeoObjectList(emptyList()))
                                return@let
                            }

                            searchSession = searchManager.submit(
                                searchField,
                                VisibleRegionUtils.toPolygon(mapValue.visibleRegion),
                                searchOptions,
                                searchSessionListener,
                            )
                        }
                    }
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
        }

        IconButton(
            onClick = {
                gpsTask.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            exception.startResolutionForResult(
                                context as Activity,
                                101
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Toast.makeText(
                                context,
                                exception.message ?: "Unexpected exception",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            exception.message ?: "Unexpected exception",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                if (checkLocationPermissions) {
                    val task = LocationServices.getFusedLocationProviderClient(context)
                    task.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            event(
                                BookingEvent.GetCurrentPosition(
                                    location.latitude,
                                    location.longitude
                                )
                            )
                        }
                    }
                }

                if (state.userPosition != null) {
                    val animation = Animation(
                        Animation.Type.SMOOTH,
                        1.5f
                    )

                    map?.move(
                        CameraPosition(
                            Point(state.userPosition.latitude, state.userPosition.longitude),
                            20f,
                            0f,
                            30.0f
                        ),
                        animation
                    ) {}
                }
            },
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    bottom = 40.dp
                )
                .size(40.dp)
                .clip(CircleShape)
                .border(width = 2.dp, color = colors.primary, shape = CircleShape)
                .align(Alignment.BottomStart),
            enabled = true,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = colors.primary,
                containerColor = colors.background
            ),
            interactionSource = remember { MutableInteractionSource() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = null,
                tint = colors.primary
            )
        }
    }
}

private val permissionList = listOf(
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION
)

private val locationRequest = LocationRequest.Builder(
    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
    10_000L
)
    .setMinUpdateIntervalMillis(5_000L)
    .build()

private val searchManager =
    SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)

@Composable
fun MapScreenLifecycleObserver(
    lifecycleOwner: LifecycleOwner,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    onStart.invoke()
                }

                Lifecycle.Event.ON_STOP -> {
                    onStop.invoke()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            searchSession?.cancel()
        }
    }
}