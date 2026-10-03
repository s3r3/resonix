package com.resonix.media.feature.player.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.media3.common.Player
import com.resonix.media.core.ui.R

@Composable
fun JumpToTimeButton(
    player: Player?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlayerButton(
        modifier = modifier,
        enabled = player != null,
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Rounded.Schedule,
            contentDescription = stringResource(R.string.jump_to_time),
        )
    }
}