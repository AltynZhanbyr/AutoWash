package com.example.autowash.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autowash.R
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors

@Composable
fun SelectedAutoWash(
    modifier: Modifier = Modifier,
    autoWashName: String,
    distance: Double,
    onBookingClick: () -> Unit
) {
    val localColors = LocalColors.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = localColors.background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp,
                    horizontal = 10.dp
                ),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = autoWashName,
                fontSize = 26.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center,
                color = localColors.onBackground,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = stringResource(R.string.placeholder_meters_away, distance),
                fontSize = 12.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                color = localColors.tint,
            )

            TextButton(
                onClick = onBookingClick,
                contentPadding = PaddingValues(vertical = 0.dp, horizontal = 0.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.lbl_booking_time),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.W600,
                    color = localColors.secondary
                )
            }
        }
    }
}

@Composable
@Preview
private fun SelectedAutoWashPrev() {
    AppPreviewTheme(
        modifier = Modifier
            .padding(5.dp)
    ) {
        SelectedAutoWash(
            modifier = Modifier
                .fillMaxWidth(),
            autoWashName = "CAR WASH WURTH",
            distance = 500.0,
        ) { }
    }
}