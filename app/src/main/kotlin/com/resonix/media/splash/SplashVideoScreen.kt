package com.resonix.media.splash

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.resonix.media.R
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFF231F20)

/**
 * Plays the silent launch video once and calls [onFinished] when it ends, fails, or when
 * [fallbackDurationMillis] elapses, so a decoder that never reports readiness cannot trap the
 * user on the splash.
 */
@OptIn(UnstableApi::class)
@Composable
fun SplashVideoScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    videoResId: Int = R.raw.resonix_splash,
    fallbackDurationMillis: Long = 4_000L,
) {
    val context = LocalContext.current
    val rootView = LocalView.current

    val player = remember(videoResId) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri("android.resource://${context.packageName}/$videoResId"))
            volume = 0f
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onFinished()
            }

            override fun onPlayerError(error: PlaybackException) {
                onFinished()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    DisposableEffect(rootView) {
        val keepScreenOn = rootView.keepScreenOn
        rootView.keepScreenOn = true
        onDispose { rootView.keepScreenOn = keepScreenOn }
    }

    LaunchedEffect(player) {
        delay(fallbackDurationMillis)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setKeepContentOnPlayerReset(true)
                    this.player = player
                }
            },
            update = { playerView -> playerView.player = player },
            onRelease = { playerView -> playerView.player = null },
        )
    }
}