package com.vidremover.presentation.viewmodel

import com.vidremover.domain.model.DuplicateGroup
import com.vidremover.domain.usecase.DetectionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuplicateStateHolder @Inject constructor() {
    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

    private val _detectionMode = MutableStateFlow(DetectionMode.BOTH)
    val detectionMode: StateFlow<DetectionMode> = _detectionMode.asStateFlow()

    fun setDuplicateGroups(groups: List<DuplicateGroup>) {
        _duplicateGroups.value = groups
    }

    fun setDetectionMode(mode: DetectionMode) {
        _detectionMode.value = mode
    }

    fun clearDuplicateGroups() {
        _duplicateGroups.value = emptyList()
    }
}
