package com.example.autowash.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.autowash.R
import com.example.autowash.ui.util.AppPreviewTheme
import com.example.autowash.util.LocalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDialog(
    visible: Boolean = false,
    title: String,
    text: String,
    dialogProperties: DialogProperties = DialogProperties(),
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String? = null
) {
    val colors = LocalColors.current

    AnimatedVisibility(
        visible = visible
    ) {
        BasicAlertDialog(
            modifier = Modifier
                .heightIn(
                    max = 380.dp
                ),
            properties = dialogProperties,
            onDismissRequest = onClose
        ) {
            Surface(
                color = colors.background,
                shape = RoundedCornerShape(15.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 5.dp, horizontal = 10.dp),
                ) {
                    Text(
                        text = title,
                        modifier = Modifier
                            .fillMaxWidth(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W500
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = text,
                        modifier = Modifier
                            .fillMaxWidth(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W400
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        BasicButton(
                            modifier = Modifier
                                .weight(1f),
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                            paddingValues = PaddingValues(),
                            onClick = onClose,
                            content = {
                                Text(
                                    text = stringResource(R.string.lbl_cancel)
                                )
                            }
                        )

                        BasicButton(
                            modifier = Modifier
                                .weight(1f),
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                            paddingValues = PaddingValues(),
                            onClick = onConfirm,
                            content = {
                                Text(
                                    text = confirmText ?: stringResource(R.string.lbl_confirm)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun NotificationDialogPrev() {
    AppPreviewTheme {
        NotificationDialog(
            visible = true,
            title = "Гео данные отключены!",
            text = "Включите данные о гелокации чтобы знать ваше местоположение",
            onClose = {

            },
            onConfirm = {

            }
        )
    }
}