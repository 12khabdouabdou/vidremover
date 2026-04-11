package com.vidremover.data.repository

import android.net.Uri
import com.vidremover.data.local.MediaStoreDataSource
import com.vidremover.data.model.toDomain
import com.vidremover.domain.model.Video
import com.vidremover.domain.model.VideoFolder
import com.vidremover.domain.usecase.ComputeMD5HashUseCase
import com.vidremover.domain.usecase.ComputePHashUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
* Implementation of VideoRepository that delegates to MediaStoreDataSource.
* Handles data access and business logic operations on videos.
*
* This repository uses constructor injection for its dependencies:
* - [MediaStoreDataSource] for MediaStore queries
* - [ComputeMD5HashUseCase] for MD5 hash computation
* - [ComputePHashUseCase] for perceptual hash computation
*
* @property dataSource The data source for MediaStore operations
* @property computeMD5HashUseCase Use case for computing MD5 hashes
* @property computePHashUseCase Use case for computing perceptual hashes
*/
@Singleton
class VideoRepository @Inject constructor(
    private val dataSource: MediaStoreDataSource,
    private val computeMD5HashUseCase: ComputeMD5HashUseCase,
    private val computePHashUseCase: ComputePHashUseCase
) : com.vidremover.domain.repository.VideoRepository {

    override suspend fun getAllVideos(): List<Video> = withContext(Dispatchers.IO) {
        dataSource.queryVideos().map { it.toDomain() }
    }

    override suspend fun getVideosFromFolders(folders: List<String>): List<Video> = withContext(Dispatchers.IO) {
        dataSource.queryVideosFromFolders(folders).map { it.toDomain() }
    }

    override suspend fun getFolders(): List<VideoFolder> = withContext(Dispatchers.IO) {
        dataSource.queryFolders().map { it.toDomain() }
    }

    override suspend fun deleteVideo(video: Video): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(video.uri)
            dataSource.deleteVideo(uri)
        } catch (e: Exception) {
            false
        }
    }

    override fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    override fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = durationMs / (1000 * 60 * 60)
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

override suspend fun computeMD5Hash(video: Video): String =
    computeMD5HashUseCase(video)

override suspend fun computePHash(video: Video): String =
    computePHashUseCase(video)

    override suspend fun getStorageFreedBytes(): Long = withContext(Dispatchers.IO) {
        // Calculate storage freed - this is a simplified version
        // In production, you'd track deleted video sizes
        0L
    }
}