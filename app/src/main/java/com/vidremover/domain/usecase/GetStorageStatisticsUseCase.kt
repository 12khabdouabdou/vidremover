package com.vidremover.domain.usecase

import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.vidremover.domain.model.StorageStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for retrieving device storage statistics.
 *
 * This use case queries the device's storage information using StatFs
 * and combines it with session-specific freed space tracking.
 *
 * @param getFreedSpace Use case for getting freed space this session
 */
@Singleton
class GetStorageStatisticsUseCase @Inject constructor(
    private val getFreedSpace: GetStorageFreedUseCase
) {
    /**
     * Retrieves current storage statistics.
     *
     * @return StorageStatistics containing device and session storage info
     */
    suspend operator fun invoke(): StorageStatistics = withContext(Dispatchers.IO) {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)

        val availableBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.availableBytes
        } else {
            @Suppress("DEPRECATION")
            stat.availableBlocksLong * stat.blockSizeLong
        }

        val totalBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.totalBytes
        } else {
            @Suppress("DEPRECATION")
            stat.blockCountLong * stat.blockSizeLong
        }

        StorageStatistics(
            freedSessionBytes = getFreedSpace(),
            availableBytes = availableBytes,
            totalBytes = totalBytes
        )
    }
}

/**
 * Use case for tracking freed space during the current session.
 * This is a simple in-memory tracker that resets on app restart.
 */
@Singleton
class GetStorageFreedUseCase @Inject constructor() {
    private var freedBytes: Long = 0L

    /**
     * Gets the total bytes freed this session.
     */
    operator fun invoke(): Long = freedBytes

    /**
     * Adds freed bytes to the session total.
     */
    fun addFreedBytes(bytes: Long) {
        freedBytes += bytes
    }

    /**
     * Resets the freed bytes counter.
     */
    fun reset() {
        freedBytes = 0L
    }
}
