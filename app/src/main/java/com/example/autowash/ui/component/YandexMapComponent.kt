package com.example.autowash.ui.component

import android.content.pm.PackageManager
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.viewpager.widget.ViewPager.LayoutParams
import com.example.autowash.R
import com.example.autowash.util.LocalColors
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

private val mapView: MapView? = null
private var mapKit: MapKit? = null

@Composable
fun YandexMapComponent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onBackPress: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = LocalColors.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                MapKitFactory.initialize(context)
                mapKit = MapKitFactory.getInstance()
            }

            if (event == Lifecycle.Event.ON_START) {
                mapKit?.onStart()
                mapView?.onStart()

            }
            if (event == Lifecycle.Event.ON_STOP) {
                mapKit?.onStop()
                mapView?.onStop()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    val locationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {}
    )

    val permissionList = listOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    LaunchedEffect(Unit) {
        val checkLocationPermissions = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!checkLocationPermissions) {
            locationPermission.launch(permissionList.toTypedArray())
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
                mapView ?: MapView(context)
            },
            update = { view ->
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
                value = searchText,
                onValueChange = onSearchChange,
                placeholder = stringResource(R.string.lbl_search),
                singleLine = true
            )
        }

        IconButton(
            onClick = {},
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