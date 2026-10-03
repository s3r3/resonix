package com.resonix.media.feature.player.buttons

import androidx.annotation.OptIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.resonix.media.core.ui.R

// ponytail: Media3 has no seekToNextFrame(); stepping approximates one frame at 25fps.
// Upgrade path: read the real frame rate from the video Format once exposed per-item and use it here.
private const val DEFAULT_FRAME_DURATION_MS = 40L

@OptIn(UnstableApi::class)
@Composable
fun FrameStepButton(
    player: Player?,
    modifier: Modifier = Modifier,
) {
    PlayerButton(
        modifier = modifier,
        enabled = player != null,
        onClick = {
            player?.let {
                it.pause()
                it.seekTo((it.currentPosition + DEFAULT_FRAME_DURATION_MS).coerceAtLeast(0L))
            }
        },
    ) {
        Icon(
            imageVector = Icons.Rounded.SkipNext,
            contentDescription = stringResource(R.string.player_controls_next_frame),
        )
    }
}