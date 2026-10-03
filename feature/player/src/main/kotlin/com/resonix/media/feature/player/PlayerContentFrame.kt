package com.resonix.media.feature.player

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import com.resonix.media.feature.player.extensions.toContentScale
import com.resonix.media.feature.player.state.PictureInPictureState
import com.resonix.media.feature.player.state.VideoZoomAndContentScaleState
import com.resonix.media.feature.player.ui.ShutterView
import com.resonix.media.feature.player.ui.SubtitleConfiguration
import com.resonix.media.feature.player.ui.SubtitleView

// ponytail: Video effects use Compose ColorMatrix on PlayerContentFrame.
// A SurfaceView renders outside the view hierarchy, so the matrix only reaches the picture when
// the frame is drawn as a TextureView; the surface is swapped only while an effect is applied.
// Upgrade path: migrate to Media3 VideoEffect processor when custom OpenGL shader pipeline is required.
@OptIn(UnstableApi::class)
@Composable
fun PlayerContentFrame(
    modifier: Modifier = Modifier,
    player: Player,
    pictureInPictureState: PictureInPictureState,
    videoZoomAndContentScaleState: VideoZoomAndContentScaleState,
    subtitleConfiguration: SubtitleConfiguration,
    contrast: Float = 1.0f,
    saturation: Float = 1.0f,
    hue: Float = 0.0f,
) {
    val presentationState = rememberPresentationState(player)
    val hasEffect = contrast != 1.0f || saturation != 1.0f || hue != 0.0f
    val colorMatrix = remember(contrast, saturation, hue) {
        ColorMatrix().apply {
            setToSaturation(saturation)
            if (hue != 0.0f) timesAssign(ColorMatrix(hueRotationMatrix(hue)))
            if (contrast != 1.0f) {
                // Apply contrast scale around midpoint
                val scale = contrast
                val translate = (-0.5f * scale + 0.5f) * 255f
                timesAssign(
                    ColorMatrix(
                        floatArrayOf(
                            scale, 0f, 0f, 0f, translate,
                            0f, scale, 0f, 0f, translate,
                            0f, 0f, scale, 0f, translate,
                            0f, 0f, 0f, 1f, 0f,
                        ),
                    ),
                )
            }
        }
    }
    Box(modifier.fillMaxSize()) {
        PlayerSurface(
            player = player,
            surfaceType = if (hasEffect) SURFACE_TYPE_TEXTURE_VIEW else SURFACE_TYPE_SURFACE_VIEW,
            modifier = Modifier
                .resizeWithContentScale(
                    contentScale = videoZoomAndContentScaleState.videoContentScale.toContentScale(),
                    sourceSizeDp = presentationState.videoSizeDp?.let { size ->
                        size.copy(
                            width = with(LocalDensity.current) { size.width.toDp().value },
                            height = with(LocalDensity.current) { size.height.toDp().value },
                        )
                    },
                )
                .onGloballyPositioned {
                    val bounds = it.boundsInWindow()
                    val rect = Rect(
                        bounds.left.toInt(),
                        bounds.top.toInt(),
                        bounds.right.toInt(),
                        bounds.bottom.toInt(),
                    )
                    pictureInPictureState.setVideoViewRect(rect)
                }
                .graphicsLayer {
                    scaleX = videoZoomAndContentScaleState.zoom
                    scaleY = videoZoomAndContentScaleState.zoom
                    translationX = videoZoomAndContentScaleState.offset.x
                    translationY = videoZoomAndContentScaleState.offset.y
                    this.colorFilter = ColorFilter.colorMatrix(colorMatrix)
                },
        )

        SubtitleView(
            player = player,
            isInPictureInPictureMode = pictureInPictureState.isInPictureInPictureMode,
            configuration = subtitleConfiguration,
        )

        if (presentationState.coverSurface) {
            ShutterView()
        }
    }
}

/**
 * Hue rotation in the YIQ colour space, the same transform Android's `ColorMatrix.setRotate`
 * applies. Compose's [ColorMatrix] has no hue helper, so the coefficients are spelled out here.
 */
private fun hueRotationMatrix(degrees: Float): FloatArray {
    val radians = Math.toRadians(degrees.toDouble())
    val cos = kotlin.math.cos(radians).toFloat()
    val sin = kotlin.math.sin(radians).toFloat()
    val red = 0.213f
    val green = 0.715f
    val blue = 0.072f
    val redShift = (1f - cos) * red
    val greenShift = (1f - cos) * green
    val blueShift = (1f - cos) * blue
    return floatArrayOf(
        red + cos + redShift, green - cos + greenShift, blue - cos + blueShift, 0f, 0f,
        red - cos - redShift, green + cos, blue - cos + blueShift, 0f, 0f,
        red - cos - redShift, green - cos - greenShift, blue + cos, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )
}
