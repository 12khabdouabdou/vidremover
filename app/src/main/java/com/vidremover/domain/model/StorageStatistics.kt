package com.vidremover.domain.model

/**
 * Data class representing device storage statistics.
 *
 * @property freedSessionBytes Total bytes freed during this app session
 * @property availableBytes Available storage space on the device
 * @property totalBytes Total storage capacity of the device
 */
data class StorageStatistics(
    val freedSessionBytes: Long,
    val availableBytes: Long,
    val totalBytes: Long
) {
    /**
     * Calculates used storage in bytes.
     */
    val usedBytes: Long
        get() = totalBytes - availableBytes

    /**
     * Calculates the percentage of storage used (0.0 - 1.0).
     */
    val usedPercentage: Float
        get() = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f

    /**
     * Calculates the percentage of storage available (0.0 - 1.0).
     */
    val availablePercentage: Float
        get() = if (totalBytes > 0) availableBytes.toFloat() / totalBytes.toFloat() else 0f

    companion object {
        /**
         * Formats bytes into human-readable string (e.g., "1.5 GB").
         */
        fun formatBytes(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
                else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            }
        }
    }
}
