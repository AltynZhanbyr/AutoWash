package com.example.autowash.feature.booking

import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.PointF
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.autowash.feature.booking.model.BookingScreens
import com.example.autowash.feature.booking.model.BookingUIState
import com.example.autowash.feature.booking.model.MapKitListeners
import com.example.autowash.ui.component.BasicButton
import com.example.autowash.ui.component.Dropdown
import com.example.autowash.ui.component.TextField
import com.example.autowash.util.LocalColors
import com.example.autowash.util.calculateDistance
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.yandex.mapkit.Animation
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraListener
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
import kotlinx.coroutines.launch

private var mapView: MapView? = null
private var mapKit: MapKit? = null
private var searchSession: Session? = null
private var placemark: PlacemarkMapObject? = null
private var userLocationLayer: UserLocationLayer? = null
private var map: Map? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    state: BookingUIState,
    paddingValues: PaddingValues,
    event: (BookingEvent) -> Unit,
    toggleDialog: (Boolean) -> Unit,
    onBackPress: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = LocalColors.current

    val scope = rememberCoroutineScope()

    var dialogVisibility by remember { mutableStateOf(false) }
    var lastReqState by remember { mutableStateOf("") }

    var localGeoObject by remember { mutableStateOf<GeoObject?>(null) }

    val searchOptions = SearchOptions().apply {
        searchTypes = SearchType.BIZ.value
        resultPageSize = 50
    }

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)

    val clientSettings = LocationServices.getSettingsClient(context)
    val gpsTask = clientSettings.checkLocationSettings(builder.build())

    var checkLocationPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

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

    val sheetState = rememberModalBottomSheetState()

    val geoObjectTapListener = mapKitListeners.geoObjectTapListener { geoObject ->
        localGeoObject = geoObject
    }

    val cameraListener = mapKitListeners.cameraListener {
        searchSession?.cancel()
        map?.let { mapValue ->
            if (lastReqState.isBlank()) {
                event(BookingEvent.SetGeoObjectList(emptyList()))
                return@let
            }

            searchSession = searchManager.submit(
                lastReqState,
                VisibleRegionUtils.toPolygon(mapValue.visibleRegion),
                searchOptions,
                searchSessionListener,
            )
        }
    }

    BackHandler(state.selectedBookingScreen.isMapScreen()) {
        clearData(geoObjectTapListener, cameraListener)
        event(BookingEvent.SetGeoObjectList(emptyList()))
        event(BookingEvent.ChangeSearch(""))
        event(BookingEvent.ChangeBookingSelectedScreen(BookingScreens.MainBookingScreen))
    }

    val locationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        if (!checkLocationPermissions) {
            locationPermission.launch(permissionList.toTypedArray())
        }
    }

    LaunchedEffect(state.selectedMapDropdown) {
        val point =
            state.selectedMapDropdown?.cityLatLong ?: state.cityMapList[0].cityLatLong
        val nePoint = state.selectedMapDropdown?.northEast ?: state.cityMapList[0].northEast
        val swPoint = state.selectedMapDropdown?.southWest ?: state.cityMapList[0].southWest

        map?.cameraBounds?.latLngBounds = BoundingBox(
            swPoint,
            nePoint
        )
        map?.move(
            CameraPosition(
                point,
                9f,
                0f,
                0f
            )
        )
    }

    MapScreenLifecycleObserver(
        lifecycleOwner = lifecycleOwner,
        onStart = {
            checkLocationPermissions = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

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
            map?.removeTapListener(geoObjectTapListener)
            map?.removeCameraListener(cameraListener)
        })

    LaunchedEffect(state.searchedGeoObjects) {
        map?.mapObjects?.clear()

        state.searchedGeoObjects.forEach { geoObject ->
            placemark = map?.mapObjects?.addPlacemark()?.apply {
                geometry = geoObject.geometry[0].point ?: Point(0.0, 0.0)
                setIcon(
                    ImageProvider.fromResource(context, R.drawable.img_car_wash),
                    IconStyle().apply {
                        anchor = PointF(0.5f, 1.0f)
                        scale = 0.03f
                        zIndex = 10f
                    }
                )
            }

            placemark?.addTapListener { _, _ ->
                localGeoObject = geoObject
                true
            }
        }
    }

    LaunchedEffect(state.userPosition) {
        map?.mapObjects?.clear()

        state.searchedGeoObjects.forEach { geoObject ->
            placemark = map?.mapObjects?.addPlacemark()?.apply {
                geometry = geoObject.geometry[0].point ?: Point(0.0, 0.0)
                setIcon(
                    ImageProvider.fromResource(context, R.drawable.img_res_final),
                    IconStyle().apply {
                        anchor = PointF(0.5f, 1.0f)
                        scale = 0.03f
                        zIndex = 10f
                    }
                )
            }

            placemark?.addTapListener { _, _ ->
                localGeoObject = geoObject
                true
            }
        }
    }

    Box(
        modifier = modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        if (localGeoObject != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    localGeoObject = null
                },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    Text(localGeoObject?.name.orEmpty())

                    Text(localGeoObject?.descriptionText.orEmpty())

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        BasicButton(
                            modifier = Modifier
                                .weight(1f),
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                            paddingValues = PaddingValues(),
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        localGeoObject = null
                                    }
                                }
                            },
                            content = {
                                Text(stringResource(R.string.lbl_cancel))
                            }
                        )

                        BasicButton(
                            modifier = Modifier
                                .weight(1f),
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                            paddingValues = PaddingValues(),
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    val geometry =
                                        localGeoObject!!.geometry[0].point ?: Point(0.0, 0.0)
                                    val distance =
                                        if (state.userPosition != null) calculateDistance(
                                            state.userPosition.latitude,
                                            state.userPosition.longitude,
                                            geometry.latitude,
                                            geometry.longitude
                                        ) else 0.0

                                    event(
                                        BookingEvent.SelectedGeoObject(
                                            localGeoObject!!,
                                            distance
                                        )
                                    )

                                    if (!sheetState.isVisible) {
                                        localGeoObject = null
                                    }

                                    clearData(geoObjectTapListener, cameraListener)
                                    event(BookingEvent.SetGeoObjectList(emptyList()))
                                    event(BookingEvent.ChangeSearch(""))
                                    event(BookingEvent.ChangeBookingSelectedScreen(BookingScreens.MainBookingScreen))
                                }
                            },
                            content = {
                                Text(stringResource(R.string.lbl_choose))
                            }
                        )
                    }
                }
            }
        }


        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = { context ->
                mapView = MapView(context)
                map = mapView!!.mapWindow.map
                map?.cameraBounds?.setMinZoomPreference(9f)

                val point =
                    state.selectedMapDropdown?.cityLatLong ?: state.cityMapList[0].cityLatLong
                val nePoint = state.selectedMapDropdown?.northEast ?: state.cityMapList[0].northEast
                val swPoint = state.selectedMapDropdown?.southWest ?: state.cityMapList[0].southWest

                map!!.isRotateGesturesEnabled = false

                map?.cameraBounds?.latLngBounds = BoundingBox(
                    swPoint,
                    nePoint
                )
                map?.move(
                    CameraPosition(
                        point,
                        9f,
                        0f,
                        0f
                    )
                )

                map?.addCameraListener(cameraListener)
                mapView!!
            },
            update = { view ->

                // map?.addTapListener(geoObjectTapListener)
                view.layoutParams.height = LayoutParams.MATCH_PARENT
                view.layoutParams.width = LayoutParams.MATCH_PARENT
            }
        )

        Column(
            modifier = Modifier
                .padding(
                    start = 9.dp,
                    end = 9.dp,
                    top = 9.dp
                )
                .fillMaxWidth(),

            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                BasicButton(
                    modifier = Modifier
                        .weight(0.1f)
                        .height(56.dp),
                    onClick = {
                        clearData(geoObjectTapListener, cameraListener)
                        event(BookingEvent.SetGeoObjectList(emptyList()))
                        event(BookingEvent.ChangeSearch(""))
                        onBackPress.invoke()
                    },
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
                    value = state.searchField,
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
                                val point =
                                    state.selectedMapDropdown?.cityLatLong
                                        ?: state.cityMapList[0].cityLatLong

                                mapValue.move(
                                    CameraPosition(
                                        point,
                                        9f,
                                        0f,
                                        0f
                                    )
                                )

                                if (state.searchField.isBlank()) {
                                    event(BookingEvent.SetGeoObjectList(emptyList()))
                                    return@let
                                }

                                searchSession = searchManager.submit(
                                    state.searchField,
                                    VisibleRegionUtils.toPolygon(mapValue.visibleRegion),
                                    searchOptions,
                                    searchSessionListener,
                                )
                            }
                        }
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    isBorderEnabled = true
                )
            }

            Row(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )

                Dropdown(
                    modifier = modifier
                        .weight(1f),
                    dropdownList = state.cityMapList,
                    selectedItem = state.selectedMapDropdown
                ) { value ->
                    if (value == 1)
                        Toast.makeText(context, "Карта города в разработке", Toast.LENGTH_SHORT)
                            .show()
                    event(BookingEvent.SelectCityMapDropdown(value))
                }
            }
        }

        IconButton(
            onClick = {
                val animation = Animation(
                    Animation.Type.SMOOTH,
                    1.5f
                )

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
                        if (location == null) return@addOnSuccessListener

                        scope.launch {
                            map?.move(
                                CameraPosition(
                                    Point(location.latitude, location.longitude),
                                    20f,
                                    0f,
                                    30.0f
                                ),
                                animation
                            ) {}

                            event(
                                BookingEvent.GetCurrentPosition(
                                    location.latitude,
                                    location.longitude
                                )
                            )
                        }
                    }
                } else toggleDialog(true)
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
    Priority.PRIORITY_HIGH_ACCURACY,
    10_000L
)
    .setMinUpdateIntervalMillis(5_000L)
    .build()

private val searchManager =
    SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)

private fun clearData(
    geoObjectTapListener: GeoObjectTapListener,
    cameraListener: CameraListener
) {
    mapKit?.onStop()
    mapView?.onStop()
    map?.removeTapListener(geoObjectTapListener)
    map?.removeCameraListener(cameraListener)
    userLocationLayer = null
    placemark = null
    searchSession?.cancel()
}

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