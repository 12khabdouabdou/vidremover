package com.vidremover.domain.repository

import com.vidremover.domain.model.DuplicateGroup
import com.vidremover.domain.model.Video
import com.vidremover.domain.model.VideoFolder
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getAllVideos(): Flow<List<Video>>
    fun getVideosInFolders(folders: List<String>): Flow<List<Video>>
    fun getVideoFolders(): Flow<List<VideoFolder>>
    suspend fun computeMD5Hash(video: Video): String
    suspend fun computepHash(video: Video): String
    suspend fun deleteVideo(video: Video): Boolean
    suspend fun getStorageFreedBytes(): Long
}