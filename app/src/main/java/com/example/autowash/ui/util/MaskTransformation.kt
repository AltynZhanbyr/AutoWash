package com.example.autowash.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MaskTransformation() : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return maskFilter(text)
    }
}

fun maskFilter(text: AnnotatedString): TransformedText {
    val original = text.text.filter { it.isDigit() }

    val trimmed = original.take(10)
    val formatted = buildString {
        append("+7 (")
        for (i in trimmed.indices) {
            when (i) {
                0, 1, 2 -> append(trimmed[i])
                3 -> append(") ").append(trimmed[i])
                4, 6 -> append(trimmed[i])
                5, 7 -> append(trimmed[i]).append(" ")
                8, 9 -> append(trimmed[i])
            }
        }
    }

    val transformedLength = formatted.length

    val numberOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return when (offset) {
                0 -> 4
                1, 2, 3 -> offset + 4
                4, 5, 6 -> offset + 6
                7, 8 -> offset + 7
                9, 10 -> offset + 8
                else -> transformedLength
            }.coerceAtMost(transformedLength)
        }

        override fun transformedToOriginal(offset: Int): Int {
            return when {
                offset < 4 -> 0
                offset in 4..6 -> offset - 4
                offset in 7..9 -> offset - 6
                offset in 10..12 -> offset - 7
                offset in 13..14 -> offset - 8
                else -> trimmed.length
            }.coerceAtMost(trimmed.length)
        }
    }

    return TransformedText(AnnotatedString(formatted), numberOffsetTranslator)
}
