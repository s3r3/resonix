package com.resonix.media.feature.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.resonix.media.core.ui.R

// ponytail: JumpToTime handles only milliseconds input in text field.
// Upgrade path: add multi-field (HH:MM:SS) input or time picker composable when design specifies.
@Composable
fun JumpToTimeDialog(
    player: Player?,
    onJump: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var textValue by remember { mutableStateOf("") }
    val durationMs = player?.duration ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.jump_to_time)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            textValue = input
                        }
                    },
                    label = { Text("Time (seconds)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            val seconds = textValue.toLongOrNull() ?: 0L
            val targetMs = seconds * 1000L
            Button(
                onClick = {
                    onJump(targetMs)
                    onDismiss()
                },
                enabled = seconds > 0 && (durationMs <= 0 || targetMs <= durationMs),
            ) {
                Text(text = stringResource(R.string.done))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
