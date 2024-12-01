package com.example.autowash.ui.util

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.autowash.ui.theme.darkColors
import com.example.autowash.ui.theme.lightColors
import com.example.autowash.util.LocalColors

@Composable
fun AppPreviewTheme(
    darkTheme: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColors
        else -> lightColors
    }

    CompositionLocalProvider(
        LocalColors provides colorScheme
    ) {
        Surface(
            modifier = modifier
                .padding(5.dp),
            color = colorScheme.background
        ) {
            content.invoke()
        }
    }
}