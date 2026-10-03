package com.resonix.media.feature.media.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.resonix.media.core.model.MediaLayoutMode
import com.resonix.media.core.ui.R

@Composable
fun MediaLayoutMode.name(): String {
    return when (this) {
        MediaLayoutMode.LIST -> stringResource(id = R.string.list)
        MediaLayoutMode.GRID -> stringResource(id = R.string.grid)
    }
}
