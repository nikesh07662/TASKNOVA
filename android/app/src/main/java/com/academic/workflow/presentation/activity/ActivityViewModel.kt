package com.academic.workflow.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.academic.workflow.data.repository.ActivityRepository
import com.academic.workflow.domain.model.ActivityCategory
import com.academic.workflow.domain.model.ActivityItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivityUiState(
    val activities: List<ActivityItem> = emptyList(),
    val selectedCategory: ActivityCategory? = null,
    val isLoading: Boolean = false
)

class ActivityViewModel(private val activityRepository: ActivityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            activityRepository.activities.collect { list ->
                _uiState.value = _uiState.value.copy(
                    activities = filterList(list, _uiState.value.selectedCategory)
                )
            }
        }
        refreshActivities()
    }

    fun selectCategory(category: ActivityCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        viewModelScope.launch {
            activityRepository.activities.collect { list ->
                _uiState.value = _uiState.value.copy(
                    activities = filterList(list, category)
                )
            }
        }
    }

    fun refreshActivities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            activityRepository.syncActivities().collect { /* cached */ }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun filterList(list: List<ActivityItem>, category: ActivityCategory?): List<ActivityItem> {
        return if (category == null) list else list.filter { it.category == category }
    }
}
