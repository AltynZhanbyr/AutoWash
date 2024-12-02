package com.example.autowash.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.autowash.core.model.Dropdown
import com.example.autowash.core.model.MapCity
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    modifier: Modifier = Modifier,
    dropdownList: List<Dropdown>,
    selectedItem: Dropdown?,
    expanded: MutableState<Boolean> = remember { mutableStateOf(false) },
    onSelect: (Int) -> Unit
) {
    val selectedText = selectedItem?.title ?: ""

    val colors = LocalColors.current

    val rotation = animateFloatAsState(
        targetValue = if (expanded.value) -90f else 0f,
        label = "rotationDegrees"
    )

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded.value,
        onExpandedChange = { _ ->
            expanded.value = !expanded.value
        },
    ) {
        TextField(
            value = selectedText,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            onValueChange = {},
            placeholder = "",
            enabled = false,
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = colors.onBackground,
                    modifier = Modifier
                        .rotate(rotation.value)
                        .size(26.dp)
                )
            },
            isBorderEnabled = true
        )

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier
                .background(color = colors.background)
                .border(
                    width = (1.5f).dp,
                    color = colors.primary,
                    RoundedCornerShape(4.dp)
                ),
        ) {
            dropdownList.forEachIndexed { idx, dropdown ->
                val textColor = selectedItem?.let {
                    if (selectedItem.idx == dropdown.idx) colors.primary
                    else colors.onBackground
                } ?: colors.onBackground

                DropdownMenuItem(
                    modifier = Modifier,
                    text = {
                        Text(
                            text = dropdown.title
                        )
                    },
                    onClick = {
                        expanded.value = false
                        onSelect(dropdown.idx)
                    },
                    colors = MenuItemColors(
                        textColor = textColor,
                        leadingIconColor = colors.onBackground,
                        trailingIconColor = colors.onBackground,
                        disabledTextColor = colors.onBackground.copy(alpha = 0.8f),
                        disabledLeadingIconColor = colors.onBackground.copy(alpha = 0.8f),
                        disabledTrailingIconColor = colors.onBackground.copy(alpha = 0.8f)
                    )
                )

                if (idx + 1 != dropdownList.size)
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = colors.primary,
                        thickness = 1.5f.dp
                    )
            }
        }
    }
}

@Preview
@Composable
private fun BookingScreenPrev() {
    AppPreviewTheme {
        Dropdown(
            modifier = Modifier
                .fillMaxWidth(),
            dropdownList = MapCity.toDropdownList(),
            selectedItem = null,
            expanded = remember { mutableStateOf(true) },
            onSelect = {}
        )
    }
}