package com.vidremover.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vidremover.domain.model.DuplicateGroup
import com.vidremover.domain.model.Video
import com.vidremover.domain.repository.VideoRepository
import com.vidremover.domain.usecase.DeleteVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Results/Review screen.
 * Manages duplicate groups display, video selection, and deletion operations.
 *
 * @property repository The video repository for accessing video data
 * @property deleteVideosUseCase Use case for deleting selected videos
 */
@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val repository: VideoRepository,
    private val deleteVideosUseCase: DeleteVideosUseCase
) : ViewModel() {

    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

    private val _selectedVideoIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedVideoIds: StateFlow<Set<Long>> = _selectedVideoIds.asStateFlow()

    private val _expandedGroups = MutableStateFlow<Set<String>>(emptySet())
    val expandedGroups: StateFlow<Set<String>> = _expandedGroups.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deletedCount = MutableStateFlow(0)
    val deletedCount: StateFlow<Int> = _deletedCount.asStateFlow()

    private val _freedSpace = MutableStateFlow(0L)
    val freedSpace: StateFlow<Long> = _freedSpace.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Sets the duplicate groups to display.
     *
     * @param groups List of duplicate groups
     */
    fun setDuplicateGroups(groups: List<DuplicateGroup>) {
        _duplicateGroups.value = groups
        // Auto-expand the first group if there are any groups
        if (groups.isNotEmpty()) {
            _expandedGroups.value = setOf(groups.first().id)
        }
    }

    /**
     * Toggles the expansion state of a duplicate group.
     *
     * @param groupId The ID of the group to toggle
     */
    fun toggleGroupExpansion(groupId: String) {
        val current = _expandedGroups.value.toMutableSet()
        if (current.contains(groupId)) {
            current.remove(groupId)
        } else {
            current.add(groupId)
        }
        _expandedGroups.value = current
    }

    /**
     * Expands all duplicate groups.
     */
    fun expandAllGroups() {
        _expandedGroups.value = _duplicateGroups.value.map { it.id }.toSet()
    }

    /**
     * Collapses all duplicate groups.
     */
    fun collapseAllGroups() {
        _expandedGroups.value = emptySet()
    }

    /**
     * Toggles the selection of a video.
     *
     * @param videoId The ID of the video to toggle
     */
    fun toggleVideoSelection(videoId: Long) {
        val current = _selectedVideoIds.value.toMutableSet()
        if (current.contains(videoId)) {
            current.remove(videoId)
        } else {
            current.add(videoId)
        }
        _selectedVideoIds.value = current
    }

    /**
     * Selects all videos in a specific group.
     *
     * @param groupId The ID of the group
     */
    fun selectAllInGroup(groupId: String) {
        val group = _duplicateGroups.value.find { it.id == groupId }
        group?.let {
            val current = _selectedVideoIds.value.toMutableSet()
            // Select all videos in the group except the first one (keep the original)
            val videosToSelect = it.videos.drop(1).map { video -> video.id }
            current.addAll(videosToSelect)
            _selectedVideoIds.value = current
        }
    }

    /**
     * Deselects all videos in a specific group.
     *
     * @param groupId The ID of the group
     */
    fun deselectAllInGroup(groupId: String) {
        val group = _duplicateGroups.value.find { it.id == groupId }
        group?.let {
            val current = _selectedVideoIds.value.toMutableSet()
            it.videos.forEach { video ->
                current.remove(video.id)
            }
            _selectedVideoIds.value = current
        }
    }

    /**
     * Selects all duplicate videos (all videos except the first in each group).
     */
    fun selectAllDuplicates() {
        val allDuplicates = _duplicateGroups.value
            .flatMap { it.videos.drop(1) }
            .map { it.id }
            .toSet()
        _selectedVideoIds.value = allDuplicates
    }

    /**
     * Clears all video selections.
     */
    fun clearSelection() {
        _selectedVideoIds.value = emptySet()
    }

    /**
     * Calculates the total storage that would be freed by deleting selected videos.
     *
     * @return Size in bytes
     */
    fun calculateSelectedSize(): Long {
        return _duplicateGroups.value
            .flatMap { it.videos }
            .filter { _selectedVideoIds.value.contains(it.id) }
            .sumOf { it.size }
    }

    /**
     * Gets the count of selected videos.
     *
     * @return Number of selected videos
     */
    fun getSelectedCount(): Int = _selectedVideoIds.value.size

    /**
     * Gets the total count of duplicate videos across all groups.
     *
     * @return Total duplicate count
     */
    fun getTotalDuplicateCount(): Int {
        return _duplicateGroups.value.sumOf { it.videos.size - 1 }
    }

    /**
     * Gets the total potential space savings if all duplicates are deleted.
     *
     * @return Size in bytes
     */
    fun getTotalPotentialSavings(): Long {
        return _duplicateGroups.value.sumOf { group ->
            // Sum all videos except the largest one in each group
            group.videos.drop(1).sumOf { it.size }
        }
    }

    /**
     * Deletes all selected videos.
     *
     * @return Number of videos successfully deleted
     */
    suspend fun deleteSelectedVideos(): Int {
        val videosToDelete = _duplicateGroups.value
            .flatMap { it.videos }
            .filter { _selectedVideoIds.value.contains(it.id) }

        if (videosToDelete.isEmpty()) return 0

        _isDeleting.value = true
        var deletedCount = 0
        var freedBytes = 0L

        try {
            videosToDelete.forEach { video ->
                val success = repository.deleteVideo(video)
                if (success) {
                    deletedCount++
                    freedBytes += video.size
                }
            }

            _deletedCount.value += deletedCount
            _freedSpace.value += freedBytes

            // Remove deleted videos from groups
            val deletedIds = videosToDelete.map { it.id }.toSet()
            _duplicateGroups.value = _duplicateGroups.value
                .map { group ->
                    DuplicateGroup(
                        id = group.id,
                        videos = group.videos.filter { it.id !in deletedIds },
                        similarity = group.similarity
                    )
                }
                .filter { it.videos.size > 1 }

            _selectedVideoIds.value = emptySet()

        } catch (e: Exception) {
            _error.value = "Failed to delete videos: ${e.message}"
        } finally {
            _isDeleting.value = false
        }

        return deletedCount
    }

    /**
     * Deletes a single video.
     *
     * @param video The video to delete
     * @return true if deletion was successful
     */
    suspend fun deleteVideo(video: Video): Boolean {
        return try {
            val success = repository.deleteVideo(video)
            if (success) {
                _freedSpace.value += video.size
                _deletedCount.value += 1

                // Remove video from groups and selections
                _duplicateGroups.value = _duplicateGroups.value
                    .map { group ->
                        DuplicateGroup(
                            id = group.id,
                            videos = group.videos.filter { it.id != video.id },
                            similarity = group.similarity
                        )
                    }
                    .filter { it.videos.size > 1 }

                _selectedVideoIds.value = _selectedVideoIds.value - video.id
            }
            success
        } catch (e: Exception) {
            _error.value = "Failed to delete video: ${e.message}"
            false
        }
    }

    /**
     * Formats file size for display.
     *
     * @param size Size in bytes
     * @return Formatted string
     */
    fun formatSize(size: Long): String = repository.formatFileSize(size)

    /**
     * Formats duration for display.
     *
     * @param durationMs Duration in milliseconds
     * @return Formatted string
     */
    fun formatDuration(durationMs: Long): String = repository.formatDuration(durationMs)

    /**
     * Clears any error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Resets the deletion statistics.
     */
    fun resetStats() {
        _deletedCount.value = 0
        _freedSpace.value = 0L
    }
}
