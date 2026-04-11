package com.vidremover.domain.repository

import com.vidremover.domain.model.Image
import com.vidremover.domain.model.ImageFolder
import com.vidremover.domain.model.Video
import com.vidremover.domain.model.VideoFolder

interface MediaRepository : VideoRepository {
    suspend fun getAllImages(): List<Image>
    suspend fun getImagesFromFolders(folders: List<String>): List<Image>
    suspend fun getImageFolders(): List<ImageFolder>
    suspend fun deleteImage(image: Image): Boolean
}
