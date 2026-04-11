package com.vidremover.data.model

import com.vidremover.domain.model.Video

/**
 * Data transfer object for video metadata from MediaStore.
 * Mirrors Video domain model but stays in data layer.
 */
data class VideoDto(
    val id: Long,
    val uri: String,
    val name: String,
    val path: String,
    val size: Long,
    val duration: Long,
    val dateAdded: Long,
    val mimeType: String,
    val folderName: String
)

/**
 * Converts VideoDto to domain Video model.
 */
fun VideoDto.toDomain(): Video = Video(
    id = id,
    uri = uri,
    name = name,
    path = path,
    size = size,
    duration = duration,
    dateAdded = dateAdded,
    mimeType = mimeType,
    folderName = folderName
)

/**
 * Converts domain Video to VideoDto.
 */
fun Video.toDto(): VideoDto = VideoDto(
    id = id,
    uri = uri,
    name = name,
    path = path,
    size = size,
    duration = duration,
    dateAdded = dateAdded,
    mimeType = mimeType,
    folderName = folderName
)
