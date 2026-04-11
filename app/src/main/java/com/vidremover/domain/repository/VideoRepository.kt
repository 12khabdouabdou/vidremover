package com.vidremover.domain.repository

import com.vidremover.domain.model.Video
import com.vidremover.domain.model.VideoFolder

/**
 * Repository interface for video operations.
 * Defines the contract for accessing and manipulating video data.
 * Implementations should handle platform-specific details while
 * this interface remains pure Kotlin with no Android dependencies.
 */
interface VideoRepository {

    /**
     * Retrieves all videos from the device storage.
     * @return List of all videos with their metadata
     */
    suspend fun getAllVideos(): List<Video>

    /**
     * Retrieves videos from specific folders.
     * @param folders List of folder paths to filter videos
     * @return List of videos within the specified folders
     */
    suspend fun getVideosFromFolders(folders: List<String>): List<Video>

    /**
     * Retrieves all folders containing videos.
     * @return List of folders with video counts
     */
    suspend fun getFolders(): List<VideoFolder>

    /**
     * Deletes a video from device storage.
     * @param video The video to delete
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deleteVideo(video: Video): Boolean

    /**
     * Computes MD5 hash for a video file.
     * Uses sampling for large files to improve performance.
     * @param video The video to hash
     * @return MD5 hash string
     */
    suspend fun computeMD5Hash(video: Video): String

    /**
     * Computes perceptual hash (pHash) for a video file.
     * Used for detecting similar content even after re-encoding.
     * @param video The video to hash
     * @return Perceptual hash string
     */
    suspend fun computePHash(video: Video): String

    /**
     * Formats file size in human-readable format.
     * @param size Size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    fun formatFileSize(size: Long): String

    /**
     * Formats duration in human-readable format.
     * @param durationMs Duration in milliseconds
     * @return Formatted string (e.g., "2:30" or "1:02:30")
     */
    fun formatDuration(durationMs: Long): String

    /**
     * Gets the total storage freed by deletions in this session.
     * @return Bytes freed
     */
    suspend fun getStorageFreedBytes(): Long
}
