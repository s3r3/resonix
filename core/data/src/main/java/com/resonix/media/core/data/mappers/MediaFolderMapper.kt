package com.resonix.media.core.data.mappers

import com.resonix.media.core.media.services.MediaFolder
import com.resonix.media.core.model.Folder

internal fun MediaFolder.toFolder() = Folder(
    name = name,
    path = path,
    dateModified = dateModified,
    totalSize = totalSize,
    totalDuration = totalDuration,
    videosCount = videosCount,
    foldersCount = foldersCount,
)
