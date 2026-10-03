package com.resonix.media.feature.media.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.resonix.media.core.model.MediaViewMode
import com.resonix.media.core.ui.R

@Composable
fun MediaViewMode.name(): String {
    return when (this) {
        MediaViewMode.VIDEOS -> stringResource(id = R.string.videos)
        MediaViewMode.FOLDERS -> stringResource(id = R.string.folders)
        MediaViewMode.FOLDER_TREE -> stringResource(id = R.string.tree)
    }
}
