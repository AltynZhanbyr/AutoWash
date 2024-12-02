package com.example.autowash.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults.MinWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.MinHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    isError: Boolean = false,
    isBorderEnabled: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val localColors = LocalColors.current
    val fieldColors = colors.copy(
        focusedContainerColor = localColors.background,
        unfocusedContainerColor = localColors.background,
        focusedTextColor = localColors.onBackground,
        unfocusedTextColor = localColors.onBackground,
        errorIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        disabledContainerColor = localColors.background
    )

    val textStyle = LocalTextStyle.current
        .copy(fontSize = 16.sp)
    Column {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .defaultMinSize(minWidth = MinWidth, minHeight = MinHeight)
                .then(
                    if (isBorderEnabled) Modifier.border(
                        width = (1.5f).dp,
                        color = localColors.primary,
                        RoundedCornerShape(15.dp)
                    ) else Modifier
                ),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle
                .copy(color = localColors.onBackground),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    contentPadding = PaddingValues(
                        start = 15.dp
                    ),
                    value = value,
                    shape = RoundedCornerShape(15.dp),
                    visualTransformation = visualTransformation,
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = textStyle.copy(localColors.tint)
                        )
                    },
                    label = null,
                    singleLine = true,
                    trailingIcon = trailingIcon,
                    isError = isError,
                    enabled = enabled,
                    interactionSource = interactionSource,
                    colors = fieldColors
                )
            }
        )
    }
}

@Composable
@Preview
private fun TextFieldPrev() {
    AppPreviewTheme(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalColors.current.primary)
            .heightIn(min = 80.dp)
    ) {
        TextField(
            value = "",
            onValueChange = {},
            placeholder = "Search",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
    }
}
