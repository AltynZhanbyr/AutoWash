@file:Suppress("NAME_SHADOWING")

package com.example.autowash.feature.booking

import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.viewpager.widget.ViewPager.LayoutParams
import com.example.autowash.R
import com.example.autowash.ui.component.SelectedAutoWash
import com.example.autowash.ui.component.TextField
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

@Composable
fun BookingScreen() {
    val viewModel = viewModel {
        BookingViewModel()
    }

    val state = viewModel.state.collectAsStateWithLifecycle().value
    BookingScreen(
        state = state,
        event = viewModel::eventHandler
    )
}

private var mapView: MapView? = null
private var mapKit: MapKit? = null

@Composable
private fun BookingScreen(
    state: BookingUIState,
    event: (BookingEvent) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }

    val context = LocalContext.current
    val locationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {}
    )

    val isLocationPermissionIsEnable = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                MapKitFactory.initialize(context)
                mapKit = MapKitFactory.getInstance()
            }

            if (event == Lifecycle.Event.ON_START) {
                mapKit?.onStart()
                lifecycle.value = Lifecycle.Event.ON_START

            }
            if (event == Lifecycle.Event.ON_STOP) {
                mapKit?.onStop();
                lifecycle.value = Lifecycle.Event.ON_STOP
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SideEffect {
        mapView?.let { view ->
            if (state.latitude.isBlank() && state.longitude.isBlank())
                return@let

            view.mapWindow.map.move(
                CameraPosition(
                    Point(state.latitude.toDouble(), state.longitude.toDouble()),
                    /* zoom = */ 17.0f,
                    /* azimuth = */ 150.0f,
                    /* tilt = */ 30.0f
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        val colors = LocalColors.current

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = colors.primary)
                .padding(
                    vertical = 14.dp,
                    horizontal = 24.dp
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .imePadding(),
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
                        .height(420.dp)
                        .clip(shape = RoundedCornerShape(5.dp))
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize(),
                        factory = { context ->
                            mapView = MapView(context)

                            mapView!!
                        },
                        update = { view ->
                            view.layoutParams.height = LayoutParams.MATCH_PARENT
                            view.layoutParams.width = LayoutParams.MATCH_PARENT

                            when (lifecycle.value) {
                                Lifecycle.Event.ON_START -> view.onStart()
                                Lifecycle.Event.ON_STOP -> view.onStart()
                                else -> Unit
                            }

                        }
                    )

                    IconButton(
                        modifier = Modifier
                            .padding(start = 5.dp, bottom = 5.dp)
                            .border(width = 2.dp, color = colors.primary, shape = CircleShape)
                            .size(40.dp)
                            .align(Alignment.BottomStart),
                        onClick = {
                            mapView?.let { view ->
                                if (!isLocationPermissionIsEnable) {
                                    locationPermission.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }

                                val locationRequest = LocationRequest.Builder(
                                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                                    10_000L
                                )
                                    .setMinUpdateIntervalMillis(5_000L)
                                    .build()

                                val builder = LocationSettingsRequest.Builder()
                                    .addLocationRequest(locationRequest)

                                val client: SettingsClient =
                                    LocationServices.getSettingsClient(context)
                                val locationTask = client.checkLocationSettings(builder.build())

                                locationTask.addOnFailureListener { exception ->
                                    if (exception is ResolvableApiException) {
                                        try {
                                            exception.startResolutionForResult(
                                                context as Activity,
                                                101
                                            )
                                        } catch (sendEx: IntentSender.SendIntentException) {

                                        }
                                    } else {

                                    }
                                }

                                mapKit?.let { kit ->
                                    val userLocation =
                                        kit.createTrafficLayer(view.mapWindow)

                                    userLocation.isTrafficVisible = listOf(1, 2, 3, 4).shuffled().last() % 2 == 0
                                    userLocation.resetTrafficStyles()
                                }

                                val task = LocationServices.getFusedLocationProviderClient(context)
                                task.lastLocation.addOnSuccessListener { location ->
                                    location?.let {
                                        event(
                                            BookingEvent.GetCurrentPosition(
                                                location.longitude.toString(),
                                                location.latitude.toString()
                                            )
                                        )
                                    }
                                }
                            }

                        },
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

                TextField(
                    value = state.searchField,
                    placeholder = stringResource(R.string.lbl_search),
                    modifier = Modifier
                        .fillMaxWidth(),
                    onValueChange = { value ->
                        event(BookingEvent.ChangeSearch(value))
                    }
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
                    autoWashName = state.latitude,
                    distance = 400.0
                ) {

                }
            }
        }
    }
}

@Preview
@Composable
private fun BookingScreenPrev() {
    AppPreviewTheme {
        BookingScreen()
    }
}