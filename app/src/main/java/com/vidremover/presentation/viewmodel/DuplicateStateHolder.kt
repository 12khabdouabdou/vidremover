package com.vidremover.presentation.viewmodel

import com.vidremover.domain.model.DuplicateGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuplicateStateHolder @Inject constructor() {
    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

    fun setDuplicateGroups(groups: List<DuplicateGroup>) {
        _duplicateGroups.value = groups
    }

    fun clearDuplicateGroups() {
        _duplicateGroups.value = emptyList()
    }
}
