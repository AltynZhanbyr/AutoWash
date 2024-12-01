package com.example.autowash.feature.booking

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autowash.core.model.DaysCalendar
import com.example.autowash.feature.booking.model.BookingEvent
import com.example.autowash.feature.booking.model.BookingScreens
import com.example.autowash.feature.booking.model.BookingUIState
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors

@Composable
fun DateTimePickerScreen(
    modifier: Modifier = Modifier,
    state: BookingUIState,
    paddingValues: PaddingValues,
    event: (BookingEvent) -> Unit
) {
    val colors = LocalColors.current
    val gridState = rememberLazyGridState()

    var currentScreen by remember { mutableIntStateOf(0) }

    BackHandler(state.selectedBookingScreen.isScheduleScreen()) {
        currentScreen = 0
        event(BookingEvent.ChangeBookingSelectedScreen(BookingScreens.MainBookingScreen))
    }

    Box(
        modifier = modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(color = colors.primary)
            .padding(24.dp)
    ) {
        IconButton(
            onClick = {
                event(BookingEvent.ChangeBookingSelectedScreen(BookingScreens.MainBookingScreen))
            },
            modifier = Modifier
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = null,
                tint = colors.background,
                modifier = Modifier
                    .size(40.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .heightIn(min = 250.dp)
                .background(color = colors.background)
                .padding(horizontal = 10.dp, vertical = 25.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Выбери день и время",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.W600,
                color = colors.secondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(state.daysCalendar) { item ->
                    DateCircle(Modifier, item, item == state.selectedDay) {

                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.8f),
                color = colors.secondary,
                thickness = 1.5.dp
            )

            Spacer(modifier = Modifier.height(40.dp))

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.times) { item ->
                    TimeRoundedBox(
                        modifier = Modifier,
                        time = item,
                        isSelected = item == state.selectedTime,
                        onSelect = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun DateCircle(
    modifier: Modifier = Modifier,
    calendar: DaysCalendar,
    isSelected: Boolean = false,
    onSelect: (DaysCalendar) -> Unit
) {
    val colors = LocalColors.current

    val bgColor: Color = if (isSelected) colors.primary.copy(alpha = 0.8f) else colors.primary
    val contentColor: Color =
        if (isSelected) colors.primary else colors.secondary.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(color = bgColor)
            .clickable {
                onSelect.invoke(calendar)
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "${calendar.dayNum}",
                fontSize = 20.sp,
                color = contentColor,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = calendar.dayName,
                fontSize = 10.sp,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TimeRoundedBox(
    modifier: Modifier = Modifier,
    time: String,
    isSelected: Boolean = false,
    onSelect: (String) -> Unit
) {
    val colors = LocalColors.current

    val bgColor: Color = if (isSelected) colors.primary.copy(alpha = 0.8f) else colors.primary
    val contentColor: Color =
        if (isSelected) colors.primary else colors.secondary.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(color = bgColor)
            .clickable {
                onSelect(time)
            }
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center),
            text = "$time:00",
            fontSize = 20.sp,
            color = contentColor,
            fontWeight = FontWeight.W600,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview
private fun DateTimePickerScreenPrev() {
    AppPreviewTheme {
        DateTimePickerScreen(
            modifier = Modifier,
            state = BookingUIState(),
            paddingValues = PaddingValues(),
            event = {}
        )
    }
}