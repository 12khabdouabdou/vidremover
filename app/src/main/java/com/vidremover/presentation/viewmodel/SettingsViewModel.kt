package com.vidremover.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vidremover.data.local.SettingsDataStore
import com.vidremover.domain.repository.VideoRepository
import com.vidremover.domain.usecase.DetectionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the app theme options.
 */
enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Data class representing user settings.
 *
 * @property detectionMode The default detection mode for duplicate scanning
 * @property pHashThreshold The similarity threshold for pHash detection (0.8 - 1.0)
 * @property theme The app theme preference
 */
data class UserSettings(
    val detectionMode: DetectionMode = DetectionMode.BOTH,
    val pHashThreshold: Float = 0.9f,
    val theme: AppTheme = AppTheme.SYSTEM
)

/**
 * ViewModel for the Settings screen.
 * Manages user preferences including detection mode, pHash threshold, and theme.
 *
 * @property application The application context
 * @property repository The video repository for accessing app data
 * @property settingsDataStore The data store for persisting settings
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val repository: VideoRepository,
    private val settingsDataStore: SettingsDataStore
) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _appVersion = MutableStateFlow("1.0.0")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    private val _totalVideos = MutableStateFlow(0)
    val totalVideos: StateFlow<Int> = _totalVideos.asStateFlow()

    private val _totalStorage = MutableStateFlow(0L)
    val totalStorage: StateFlow<Long> = _totalStorage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSettings()
        loadAppInfo()
    }

    /**
     * Loads settings from DataStore.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { storedSettings ->
                _settings.value = storedSettings
            }
        }
    }

    /**
     * Loads app information including version and storage statistics.
     */
    private fun loadAppInfo() {
        viewModelScope.launch {
            try {
                // Load version from package info
                val packageInfo = getApplication<Application>().packageManager
                    .getPackageInfo(getApplication<Application>().packageName, 0)
                _appVersion.value = packageInfo.versionName ?: "1.0.0"
            } catch (e: Exception) {
                _appVersion.value = "1.0.0"
            }

            // Load video and storage statistics
            try {
                val videos = repository.getAllVideos()
                _totalVideos.value = videos.size
                _totalStorage.value = videos.sumOf { it.size }
            } catch (e: Exception) {
                // Ignore errors for statistics
            }
        }
    }

    /**
     * Updates the default detection mode.
     *
     * @param mode The detection mode to set as default
     */
    fun setDetectionMode(mode: DetectionMode) {
        viewModelScope.launch {
            settingsDataStore.saveDetectionMode(mode)
            _settings.value = _settings.value.copy(detectionMode = mode)
        }
    }

    /**
     * Updates the pHash threshold.
     *
     * @param threshold The similarity threshold (0.8 - 1.0)
     */
    fun setPHashThreshold(threshold: Float) {
        val clampedThreshold = threshold.coerceIn(0.8f, 1.0f)
        viewModelScope.launch {
            settingsDataStore.savePHashThreshold(clampedThreshold)
            _settings.value = _settings.value.copy(pHashThreshold = clampedThreshold)
        }
    }

    /**
     * Updates the app theme.
     *
     * @param theme The theme to set
     */
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsDataStore.saveTheme(theme)
            _settings.value = _settings.value.copy(theme = theme)
        }
    }

    /**
     * Resets all settings to their default values.
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            settingsDataStore.resetToDefaults()
            _settings.value = UserSettings()
        }
    }

    /**
     * Refreshes app statistics.
     */
    fun refreshStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val videos = repository.getAllVideos()
                _totalVideos.value = videos.size
                _totalStorage.value = videos.sumOf { it.size }
            } catch (e: Exception) {
                // Ignore errors
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Gets a user-friendly description of the detection mode.
     *
     * @param mode The detection mode
     * @return Description string
     */
    fun getDetectionModeDescription(mode: DetectionMode): String {
        return when (mode) {
            DetectionMode.MD5_ONLY -> "Fast exact duplicate detection"
            DetectionMode.PHASH_ONLY -> "Similar content detection (slower)"
            DetectionMode.BOTH -> "Combined detection (recommended)"
        }
    }

    /**
     * Gets a user-friendly description of the theme.
     *
     * @param theme The app theme
     * @return Description string
     */
    fun getThemeDescription(theme: AppTheme): String {
        return when (theme) {
            AppTheme.LIGHT -> "Light"
            AppTheme.DARK -> "Dark"
            AppTheme.SYSTEM -> "Follow system"
        }
    }

    /**
     * Formats storage size for display.
     *
     * @param bytes Size in bytes
     * @return Formatted string
     */
    fun formatStorage(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
