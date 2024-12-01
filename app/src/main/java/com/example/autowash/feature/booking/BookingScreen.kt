package com.example.autowash.feature.booking

import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.PointF
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.viewpager.widget.ViewPager.LayoutParams
import com.example.autowash.R
import com.example.autowash.feature.booking.model.BookingEvent
import com.example.autowash.feature.booking.model.BookingScreens
import com.example.autowash.feature.booking.model.BookingUIState
import com.example.autowash.feature.booking.model.MapKitListeners
import com.example.autowash.openAppSettings
import com.example.autowash.ui.component.BasicButton
import com.example.autowash.ui.component.NotificationDialog
import com.example.autowash.ui.component.SelectedAutoWash
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors
import com.example.autowash.util.calculateDistance
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
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
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookingScreen() {
    val viewModel = koinViewModel<BookingViewModel>()

    val state = viewModel.state.collectAsStateWithLifecycle().value

    BookingScreen(
        state = state,
        event = viewModel::eventHandler
    )
}

private var mapView: MapView? = null
private var searchSession: Session? = null
private var placemark: PlacemarkMapObject? = null
private var map: Map? = null
private var mapKit: MapKit? = null

@Composable
private fun BookingScreen(
    state: BookingUIState,
    event: (BookingEvent) -> Unit
) {
    var dialogVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    NotificationDialog(
        visible = dialogVisible,
        text = stringResource(R.string.lbl_geo_loc_disabled_text),
        title = stringResource(R.string.lbl_geo_loc_disabled_title),
        onConfirm = {
            (context as Activity).openAppSettings()
            dialogVisible = false
        },
        onClose = {
            dialogVisible = false
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.selectedBookingScreen,
            label = "Booking screen state"
        ) { targetState ->
            when (targetState) {
                BookingScreens.MapScreen -> MapScreen(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = state,
                    event = remember {
                        { value ->
                            event.invoke(value)
                        }
                    },
                    paddingValues = paddingValues,
                    onBackPress = remember {
                        {
                            event(BookingEvent.ChangeBookingSelectedScreen(BookingScreens.MainBookingScreen))
                        }
                    },
                    toggleDialog = { visibility ->
                        dialogVisible = visibility
                    }
                )

                BookingScreens.MainBookingScreen -> MainBookingScreen(
                    state = state,
                    paddingValues = paddingValues,
                    event = event,
                    toggleDialog = { visibility ->
                        dialogVisible = visibility
                    }
                )
            }
        }
    }
}

@Composable
private fun MainBookingScreen(
    state: BookingUIState,
    paddingValues: PaddingValues,
    event: (BookingEvent) -> Unit,
    toggleDialog: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()

    val colors = LocalColors.current

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    val locationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {}
    )

    val mapKitListeners = MapKitListeners(context)
    val searchSessionListener = mapKitListeners.searchListener(
        result = { geoObjects, _ ->
            scope.launch {
                if (state.userPosition == null || geoObjects.isEmpty()) return@launch

                val nearestGeoPair = withContext(Dispatchers.Default) {
                    geoObjects.map { geoObject ->
                        val geometry = geoObject.geometry[0].point ?: Point(0.0, 0.0)

                        Pair(
                            geoObject,
                            calculateDistance(
                                geometry.latitude,
                                geometry.longitude,
                                state.userPosition.latitude,
                                state.userPosition.longitude
                            )
                        )
                    }.sortedBy { it.second }[0]
                }

                event(BookingEvent.SelectedGeoObject(nearestGeoPair.first, nearestGeoPair.second))
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!checkLocationPermissions) {
            locationPermission.launch(permissionList.toTypedArray())
        }
    }

    LaunchedEffect(state.selectedGeoObject) {
        map?.mapObjects?.clear()

        state.selectedGeoObject?.let { geoObject ->
            placemark = map?.mapObjects?.addPlacemark()?.apply {
                geometry = Point(geoObject.latitude.toDouble(), geoObject.longitude.toDouble())
                setIcon(
                    ImageProvider.fromResource(context, R.drawable.img_res_final),
                    IconStyle().apply {
                        anchor = PointF(0.5f, 1.0f)
                        scale = 0.03f
                        zIndex = 10f
                    }
                )
                setText(state.selectedGeoObject.title)

                map?.move(
                    CameraPosition(geometry, 17f, 0f, 0f)
                )
            }
        }

        searchSession?.cancel()
        searchSession = null
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (mapKit == null) mapKit = MapKitFactory.getInstance()

                mapKit?.onStart()
                mapView?.onStart()
                scope.launch {
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

                            event(
                                BookingEvent.GetCurrentPosition(
                                    location.latitude,
                                    location.longitude
                                )
                            )
                        }
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(color = colors.primary)
            .padding(
                vertical = 14.dp,
                horizontal = 24.dp
            )
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                text = stringResource(R.string.lbl_choose_closest_car_wash),
                fontSize = 18.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(color = colors.background),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(5.dp)
                            .clip(shape = RoundedCornerShape(5.dp)),
                        factory = { context ->
                            mapView = MapView(context)
                            map = mapView!!.mapWindow.map
                            map!!.isTiltGesturesEnabled = false
                            map!!.isZoomGesturesEnabled = false
                            map!!.isRotateGesturesEnabled = false
                            map!!.isScrollGesturesEnabled = false

                            val point = state.selectedMapDropdown?.cityLatLong
                                ?: state.cityMapList[0].cityLatLong
                            map?.move(
                                CameraPosition(
                                    point,
                                    10f,
                                    0f,
                                    0f
                                )
                            )

                            mapView!!
                        },
                        update = { view ->
                            view.layoutParams.height = LayoutParams.MATCH_PARENT
                            view.layoutParams.width = LayoutParams.MATCH_PARENT
                        }
                    )

                    BasicButton(
                        text = "Выбрать на карте",
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .height(40.dp)
                            .fillMaxSize(),
                        onClick = {
                            event(BookingEvent.ChangeBookingSelectedScreen(BookingScreens.MapScreen))
                        },
                        containerColor = colors.primary,
                        contentColor = colors.background,
                        paddingValues = PaddingValues()
                    )
                }
            }

            BasicButton(
                text = "Выбрать близкий вариант",
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .height(56.dp)
                    .fillMaxSize(),
                onClick = {
                    if (checkLocationPermissions) {
                        searchSession?.cancel()

                        val task = LocationServices.getFusedLocationProviderClient(context)
                        task.lastLocation.addOnSuccessListener { location ->
                            if (location == null) return@addOnSuccessListener

                            event(
                                BookingEvent.GetCurrentPosition(
                                    location.latitude,
                                    location.longitude
                                )
                            )
                        }

                        searchSession = searchManager.submit(
                            "Автомойка",
                            VisibleRegionUtils.toPolygon(map!!.visibleRegion),
                            searchOptions,
                            searchSessionListener
                        )
                    } else toggleDialog(true)
                },
                containerColor = colors.onPrimary,
                contentColor = colors.primary,
                paddingValues = PaddingValues()
            )

            Text(
                text = stringResource(R.string.lbl_close_to_you),
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier
            )

            SelectedAutoWash(
                modifier = Modifier
                    .fillMaxWidth(),
                autoWashName = state.selectedGeoObject?.title.orEmpty(),
                distance = state.selectedGeoObject?.distance ?: 0.0
            ) {

            }
        }
    }
}

private val locationRequest = LocationRequest.Builder(
    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
    10_000L
)
    .setMinUpdateIntervalMillis(5_000L)
    .build()

private val permissionList = listOf(
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION
)

private val searchManager =
    SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)

private val searchOptions = SearchOptions().apply {
    searchTypes = SearchType.BIZ.value
    resultPageSize = 80
}

@Preview
@Composable
private fun BookingScreenPrev() {
    AppPreviewTheme {
        BookingScreen()
    }
}