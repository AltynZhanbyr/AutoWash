package com.example.autowash.feature.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autowash.R
import com.example.autowash.ui.component.SelectedAutoWash
import com.example.autowash.ui.component.TextField
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors

@Composable
fun BookingScreen() {
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
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    text = stringResource(R.string.lbl_choose_closest_car_wash),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier
                )

                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .align(Alignment.CenterHorizontally)
                )

                TextField(
                    value = "",
                    placeholder = stringResource(R.string.lbl_search),
                    modifier = Modifier
                        .fillMaxWidth(),
                    onValueChange = {}
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