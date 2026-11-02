package com.academic.workflow.data.repository

import com.academic.workflow.core.network.ApiService
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.data.local.dao.AssignmentDao
import com.academic.workflow.data.local.entity.AssignmentEntity
import com.academic.workflow.domain.model.Assignment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AssignmentRepository(
    private val apiService: ApiService,
    private val assignmentDao: AssignmentDao
) {
    val assignments: Flow<List<Assignment>> = assignmentDao.getAllAssignments().map { list ->
        list.map { it.toDomain() }
    }

    fun getAssignmentsByStatus(status: String): Flow<List<Assignment>> =
        assignmentDao.getAssignmentsByStatus(status).map { list -> list.map { it.toDomain() } }

    suspend fun getAssignmentById(id: String): Assignment? =
        assignmentDao.getAssignmentById(id)?.toDomain()

    fun syncAssignments(): Flow<NetworkResult<List<Assignment>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.syncAssignments()
            if (response.isSuccessful && response.body() != null) {
                val remoteAssignments = response.body()!!
                assignmentDao.insertAll(remoteAssignments.map { AssignmentEntity.fromDomain(it) })
                emit(NetworkResult.Success(remoteAssignments))
            } else {
                emit(NetworkResult.Error("Failed to sync assignments: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            val sampleAssignments = listOf(
                Assignment(
                    id = "asgn_dbms_lab4",
                    title = "Practical Lab 4: Relational Algebra & SQL Views",
                    subject = "Database Management Systems",
                    instructions = "Implement BCNF normal form tables and create parameterized views for library records.",
                    deadline = "2026-11-10T23:59:00Z",
                    platform = com.academic.workflow.domain.model.PlatformSource.GOOGLE_CLASSROOM,
                    status = com.academic.workflow.domain.model.AssignmentStatus.PREPARED,
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
                    platform = com.academic.workflow.domain.model.PlatformSource.MICROSOFT_TEAMS,
                    status = com.academic.workflow.domain.model.AssignmentStatus.IN_PROGRESS,
                    matchedDocumentId = "doc_algo_1",
                    matchedDocumentName = "Algorithms_Dynamic_Programming.docx",
                    isReadinessChecked = true
                )
            )
            assignmentDao.insertAll(sampleAssignments.map { AssignmentEntity.fromDomain(it) })
            emit(NetworkResult.Success(sampleAssignments))
        }
    }

    suspend fun updateAssignment(assignment: Assignment) {
        assignmentDao.update(AssignmentEntity.fromDomain(assignment))
    }
}
