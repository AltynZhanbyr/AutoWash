@file:Suppress("NAME_SHADOWING")

package com.example.autowash.feature.booking

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.viewpager.widget.ViewPager.LayoutParams
import com.example.autowash.R
import com.example.autowash.ui.component.BasicButton
import com.example.autowash.ui.component.SelectedAutoWash
import com.example.autowash.ui.component.TextField
import com.example.autowash.ui.component.YandexMapComponent
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.SettingsClient
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

@Composable
private fun BookingScreen(
    state: BookingUIState,
    event: (BookingEvent) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START)
                mapView?.onStart()
            if (event == Lifecycle.Event.ON_STOP)
                mapView?.onStop()
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(state.selectedBookingScreen.isMapScreen()) {
        event(BookingEvent.ChangeBookingSelectedScreen(BookingScreens.MainBookingScreen))
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.selectedBookingScreen,
            label = "Booking screen state"
        ) { targetState ->
            when (targetState) {
                BookingScreens.MapScreen -> YandexMapComponent(
                    modifier = Modifier
                        .fillMaxSize(),
                    paddingValues = paddingValues,
                    state.searchField,
                    onSearchChange = { value ->
                        event(BookingEvent.ChangeSearch(value))
                    },
                    onBackPress = {
                        event(BookingEvent.ChangeBookingSelectedScreen(BookingScreens.MainBookingScreen))
                    }
                )

                BookingScreens.MainBookingScreen -> MainBookingScreen(
                    state = state,
                    paddingValues = paddingValues,
                    event = event
                )
            }
        }
    }
}

@Composable
private fun MainBookingScreen(
    state: BookingUIState,
    paddingValues: PaddingValues,
    event: (BookingEvent) -> Unit
) {
    val colors = LocalColors.current

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
                    .height(300.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .background(color = colors.background),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(5.dp)
                            .clip(shape = RoundedCornerShape(5.dp)),
                        factory = { context ->
                            mapView = MapView(context)
                            mapView!!
                        },
                        update = { view ->
                            view.layoutParams.height = LayoutParams.MATCH_PARENT
                            view.layoutParams.width = LayoutParams.MATCH_PARENT
                            view.setNoninteractive(true)
                        }
                    )

                    BasicButton(
                        text = "Выбрать на карте",
                        modifier = Modifier
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                            .height(35.dp)
                            .fillMaxSize(),
                        onClick = {
                            event(BookingEvent.ChangeBookingSelectedScreen(BookingScreens.MapScreen))
                        },
                        containerColor = colors.secondary,
                        contentColor = colors.background,
                        paddingValues = PaddingValues()
                    )
                }

//                    IconButton(
//                        modifier = Modifier
//                            .padding(start = 5.dp, bottom = 5.dp)
//                            .border(width = 2.dp, color = colors.primary, shape = CircleShape)
//                            .size(40.dp)
//                            .align(Alignment.BottomStart),
//                        onClick = {
//                            mapView?.let { view ->
//                                if (!isLocationPermissionIsEnable) {
//                                    locationPermission.launch(
//                                        arrayOf(
//                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
//                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
//                                        )
//                                    )
//                                }
//
//                                val locationRequest = LocationRequest.Builder(
//                                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
//                                    10_000L
//                                )
//                                    .setMinUpdateIntervalMillis(5_000L)
//                                    .build()
//
//                                val builder = LocationSettingsRequest.Builder()
//                                    .addLocationRequest(locationRequest)
//
//                                val client: SettingsClient =
//                                    LocationServices.getSettingsClient(LocalContext.current)
//                                val locationTask = client.checkLocationSettings(builder.build())
//
//                                locationTask.addOnFailureListener { exception ->
//                                    if (exception is ResolvableApiException) {
//                                        try {
//                                            exception.startResolutionForResult(
//                                                context as Activity,
//                                                101
//                                            )
//                                        } catch (sendEx: IntentSender.SendIntentException) {
//
//                                        }
//                                    } else {
//
//                                    }
//                                }
//
//                                mapKit?.let { kit ->
//                                    val userLocation =
//                                        kit.createTrafficLayer(view.mapWindow)
//
//                                    userLocation.isTrafficVisible = listOf(1, 2, 3, 4).shuffled().last() % 2 == 0
//                                    userLocation.resetTrafficStyles()
//                                }
//
//                                val task = LocationServices.getFusedLocationProviderClient(context)
//                                task.lastLocation.addOnSuccessListener { location ->
//                                    location?.let {
//                                        event(
//                                            BookingEvent.GetCurrentPosition(
//                                                location.longitude.toString(),
//                                                location.latitude.toString()
//                                            )
//                                        )
//                                    }
//                                }
//                            }
//
//                        },
//                        colors = IconButtonDefaults.iconButtonColors(
//                            contentColor = colors.primary,
//                            containerColor = colors.background
//                        ),
//                        interactionSource = remember { MutableInteractionSource() }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_location),
//                            contentDescription = null,
//                            tint = colors.primary
//                        )
//                    }
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

@Preview
@Composable
private fun BookingScreenPrev() {
    AppPreviewTheme {
        BookingScreen()
    }
}