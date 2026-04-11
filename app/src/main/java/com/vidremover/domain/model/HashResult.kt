package com.vidremover.domain.model

/**
 * Represents the result of computing a hash for a video.
 * Used for duplicate detection algorithms.
 *
 * @param videoId The unique identifier of the video
 * @param hashValue The computed hash string
 * @param hashType The type of hash algorithm used (MD5 or PHASH)
 */
data class HashResult(
    val videoId: Long,
    val hashValue: String,
    val hashType: HashType
)

/**
 * Enum representing the available hash algorithms for duplicate detection.
 */
enum class HashType {
    /**
     * MD5 hash - Fast exact duplicate detection.
     * Uses file content sampling to compute hash.
     */
    MD5,

    /**
     * Perceptual hash (pHash) - Similar content detection.
     * Can detect videos that are visually similar even after re-encoding or compression.
     */
    PHASH
}
