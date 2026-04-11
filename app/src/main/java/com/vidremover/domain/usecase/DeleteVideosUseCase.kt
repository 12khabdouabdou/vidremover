package com.vidremover.domain.usecase

import com.vidremover.domain.model.Video
import com.vidremover.domain.repository.VideoRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a batch delete operation.
 *
 * @property deletedCount Number of videos successfully deleted
 * @property failedCount Number of videos that failed to delete
 * @property freedBytes Total bytes freed by the operation
 */
data class DeleteResult(
    val deletedCount: Int,
    val failedCount: Int,
    val freedBytes: Long
)

/**
 * Use case for deleting multiple videos in a batch operation.
 *
 * This use case handles the deletion of multiple videos and returns
 * a summary of the operation including success/failure counts and
 * total storage freed.
 *
 * @property repository The video repository for deletion operations
 */
@Singleton
class DeleteVideosUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    /**
     * Deletes a list of videos.
     *
     * @param videos The list of videos to delete
     * @return DeleteResult containing the operation summary
     */
    suspend operator fun invoke(videos: List<Video>): DeleteResult {
        var deletedCount = 0
        var freedBytes = 0L
        val failed = mutableListOf<Video>()

        videos.forEach { video ->
            try {
                val success = repository.deleteVideo(video)
                if (success) {
                    deletedCount++
                    freedBytes += video.size
                } else {
                    failed.add(video)
                }
            } catch (e: Exception) {
                failed.add(video)
            }
        }

        return DeleteResult(
            deletedCount = deletedCount,
            failedCount = failed.size,
            freedBytes = freedBytes
        )
    }

    /**
     * Deletes a single video.
     *
     * @param video The video to delete
     * @return true if deletion was successful
     */
    suspend fun deleteSingle(video: Video): Boolean {
        return try {
            repository.deleteVideo(video)
        } catch (e: Exception) {
            false
        }
    }
}
