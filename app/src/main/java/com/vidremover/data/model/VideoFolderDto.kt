package com.vidremover.data.model

import com.vidremover.domain.model.VideoFolder

/**
 * Data transfer object for video folder information.
 */
data class VideoFolderDto(
    val name: String,
    val path: String,
    val videoCount: Int
)

/**
 * Converts VideoFolderDto to domain VideoFolder model.
 */
fun VideoFolderDto.toDomain(): VideoFolder = VideoFolder(
    name = name,
    path = path,
    videoCount = videoCount
)

/**
 * Converts domain VideoFolder to VideoFolderDto.
 */
fun VideoFolder.toDto(): VideoFolderDto = VideoFolderDto(
    name = name,
    path = path,
    videoCount = videoCount
)
