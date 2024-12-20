package com.example.autowash.feature.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autowash.R
import com.example.autowash.feature.booking.navigateToBookingScreen
import com.example.autowash.ui.component.BasicButton
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors
import com.example.autowash.util.LocalNavHost

@Composable
fun MainScreen() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        val colors = LocalColors.current
        val localNavHost = LocalNavHost.current

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = colors.background)
                .padding(9.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_app_logo_svg),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(300.dp)
                )

                Text(
                    text = stringResource(R.string.lbl_greetings_msg),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W600,
                    color = colors.secondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                BasicButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    text = stringResource(R.string.lbl_booking_now),
                    containerColor = colors.primary,
                    contentColor = colors.onBackground,
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W400
                    ),
                    paddingValues = PaddingValues(
                        vertical = 10.dp,
                        horizontal = 20.dp
                    ),
                    onClick = {
                        localNavHost.navigateToBookingScreen()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun MainScreenPrev() {
    AppPreviewTheme {
        MainScreen()
    }
}