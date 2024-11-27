package com.example.autowash.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autowash.R
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors

@Composable
fun BasicButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    contentColor: Color,
    containerColor: Color,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    paddingValues: PaddingValues
) {
    Button(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = contentColor,
            containerColor = containerColor
        ),
        contentPadding = paddingValues
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun BasicButtonPrev() {
    AppPreviewTheme {
        BasicButton(
            modifier = Modifier
                .padding(5.dp)
                .width(200.dp),
            fontWeight = FontWeight.W400,
            onClick = {},
            text = stringResource(R.string.lbl_booking_now),
            containerColor = LocalColors.current.primary,
            contentColor = LocalColors.current.onBackground,
            paddingValues = PaddingValues(
                vertical = 4.dp,
                horizontal = 10.dp
            )
        )
    }
}