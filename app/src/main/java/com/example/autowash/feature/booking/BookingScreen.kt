package com.example.autowash.feature.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.example.autowash.ui.component.SelectedAutoWash
import com.example.autowash.ui.component.TextField
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors
import com.yandex.mapkit.MapKitFactory
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

@Composable
private fun BookingScreen(
    state: BookingUIState,
    event: (BookingEvent) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE)
                MapKitFactory.initialize(context)

            if (event == Lifecycle.Event.ON_START) {
                MapKitFactory.getInstance().onStart()
                lifecycle.value = Lifecycle.Event.ON_START
            }
            if (event == Lifecycle.Event.ON_STOP) {
                MapKitFactory.getInstance().onStop()
                lifecycle.value = Lifecycle.Event.ON_STOP
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    factory = { context ->
                        MapView(context)
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
                    autoWashName = "Example",
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