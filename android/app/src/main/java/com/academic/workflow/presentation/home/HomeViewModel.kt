package com.academic.workflow.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.data.repository.ActivityRepository
import com.academic.workflow.data.repository.AssignmentRepository
import com.academic.workflow.data.repository.AttendanceRepository
import com.academic.workflow.domain.model.ActivityCategory
import com.academic.workflow.domain.model.ActivityItem
import com.academic.workflow.domain.model.Assignment
import com.academic.workflow.domain.model.AssignmentStatus
import com.academic.workflow.domain.model.AttendanceSummary
import com.academic.workflow.domain.model.PlatformSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant

data class HomeUiState(
    val isLoading: Boolean = false,
    val pendingAssignments: List<Assignment> = emptyList(),
    val urgentDeadlines: List<Assignment> = emptyList(),
    val attendanceSummary: AttendanceSummary? = null,
    val recentActivities: List<ActivityItem> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val assignmentRepository: AssignmentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            isLoading = false,
            attendanceSummary = AttendanceSummary(
                overallPercentage = 82.1f,
                subjectsShortageCount = 1,
                totalLecturesOverall = 106,
                totalAttendedOverall = 87
            ),
            pendingAssignments = listOf(
                Assignment(
                    id = "asgn_dbms_lab4",
                    title = "Practical Lab 4: Relational Algebra & SQL Views",
                    subject = "Database Management Systems",
                    instructions = "Implement BCNF normal form tables and create parameterized views for library records.",
                    deadline = "2026-11-10T23:59:00Z",
                    platform = PlatformSource.GOOGLE_CLASSROOM,
                    status = AssignmentStatus.PREPARED,
                    matchedDocumentId = "doc_dbms_1",
                    matchedDocumentName = "DBMS_Practical_Guide.pdf",
                    isReadinessChecked = true
                ),
                Assignment(
                    id = "asgn_algo_dp",
                    title = "Assignment 3: Dynamic Programming & Shortest Paths",
                    subject = "Algorithms & Data Structures",
                    instructions = "Implement Floyd-Warshall and Bellman-Ford algorithms with time complexity analysis.",
                    deadline = "2026-11-14T23:59:00Z",
                    platform = PlatformSource.MICROSOFT_TEAMS,
                    status = AssignmentStatus.IN_PROGRESS,
                    matchedDocumentId = "doc_algo_1",
                    matchedDocumentName = "Algorithms_Dynamic_Programming.docx",
                    isReadinessChecked = true
                )
            ),
            urgentDeadlines = listOf(
                Assignment(
                    id = "asgn_dbms_lab4",
                    title = "Practical Lab 4: Relational Algebra & SQL Views",
                    subject = "Database Management Systems",
                    instructions = "Implement BCNF normal form tables and create parameterized views for library records.",
                    deadline = "2026-11-10T23:59:00Z",
                    platform = PlatformSource.GOOGLE_CLASSROOM,
                    status = AssignmentStatus.PREPARED,
                    matchedDocumentId = "doc_dbms_1",
                    matchedDocumentName = "DBMS_Practical_Guide.pdf",
                    isReadinessChecked = true
                )
            ),
            recentActivities = listOf(
                ActivityItem(
                    id = "act_init",
                    title = "Connected to Google Workspace",
                    description = "Synchronized active courses from Google Classroom and Drive.",
                    category = ActivityCategory.SYNC_OPERATION,
                    timestamp = Instant.now().toString()
                )
            )
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        refreshAll()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                assignmentRepository.assignments,
                activityRepository.activities
            ) { assignments, activities ->
                if (assignments.isNotEmpty()) {
                    val pending = assignments.filter { it.status != AssignmentStatus.SUBMITTED }
                    val urgent = pending.take(3)
                    _uiState.value.copy(
                        pendingAssignments = pending,
                        urgentDeadlines = urgent,
                        recentActivities = if (activities.isNotEmpty()) activities.take(5) else _uiState.value.recentActivities
                    )
                } else {
                    _uiState.value
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            // Concurrent non-blocking background sync
            launch {
                assignmentRepository.syncAssignments().collect { /* cached in room */ }
            }
            launch {
                attendanceRepository.syncAttendance().collect { result ->
                    if (result is NetworkResult.Success) {
                        _uiState.value = _uiState.value.copy(attendanceSummary = result.data)
                    }
                }
            }
            launch {
                activityRepository.syncActivities().collect { /* cached in room */ }
            }
        }
    }
}
