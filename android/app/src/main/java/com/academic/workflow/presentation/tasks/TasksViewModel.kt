package com.academic.workflow.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.academic.workflow.data.repository.AssignmentRepository
import com.academic.workflow.domain.model.Assignment
import com.academic.workflow.domain.model.AssignmentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL,
    PENDING,
    READY_TO_SUBMIT,
    SUBMITTED,
    OVERDUE
}

data class TasksUiState(
    val assignments: List<Assignment> = emptyList(),
    val filteredAssignments: List<Assignment> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val isCalendarView: Boolean = false,
    val searchQuery: String = ""
)

class TasksViewModel(private val assignmentRepository: AssignmentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            assignmentRepository.assignments.collect { list ->
                _uiState.value = _uiState.value.copy(
                    assignments = list,
                    filteredAssignments = applyFilter(list, _uiState.value.selectedFilter, _uiState.value.searchQuery)
                )
            }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            filteredAssignments = applyFilter(_uiState.value.assignments, filter, _uiState.value.searchQuery)
        )
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredAssignments = applyFilter(_uiState.value.assignments, _uiState.value.selectedFilter, query)
        )
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(
            isCalendarView = !_uiState.value.isCalendarView
        )
    }

    private fun applyFilter(list: List<Assignment>, filter: TaskFilter, query: String): List<Assignment> {
        val matchesQuery = if (query.isBlank()) list else list.filter {
            it.title.contains(query, ignoreCase = true) || it.subject.contains(query, ignoreCase = true)
        }

        return when (filter) {
            TaskFilter.ALL -> matchesQuery
            TaskFilter.PENDING -> matchesQuery.filter { it.status == AssignmentStatus.PENDING || it.status == AssignmentStatus.IN_PROGRESS }
            TaskFilter.READY_TO_SUBMIT -> matchesQuery.filter { it.status == AssignmentStatus.PREPARED }
            TaskFilter.SUBMITTED -> matchesQuery.filter { it.status == AssignmentStatus.SUBMITTED }
            TaskFilter.OVERDUE -> matchesQuery.filter { it.status == AssignmentStatus.OVERDUE }
        }
    }
}
