package com.resonix.media.feature.media.screens

import com.resonix.media.core.model.Folder

sealed interface MediaState {
    data object Loading : MediaState
    data class Success(val data: Folder?) : MediaState
}
