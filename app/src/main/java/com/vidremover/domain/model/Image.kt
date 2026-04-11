package com.vidremover.domain.model

data class Image(
    override val id: Long,
    override val uri: String,
    override val name: String,
    override val path: String,
    override val size: Long,
    override val dateAdded: Long,
    override val mimeType: String,
    override val folderName: String,
    val width: Int,
    val height: Int
) : MediaItem

data class ImageFolder(
    val name: String,
    val path: String,
    val imageCount: Int
)
