package com.academic.workflow.data.repository

import com.academic.workflow.core.network.ApiService
import com.academic.workflow.core.network.ConfirmSubmissionRequest
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.core.network.PrepareSubmissionRequest
import com.academic.workflow.core.network.SubmissionResultResponse
import com.academic.workflow.domain.model.PreparedSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SubmissionRepository(private val apiService: ApiService) {

    fun prepareSubmission(
        assignmentId: String,
        selectedDocumentId: String
    ): Flow<NetworkResult<PreparedSubmission>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.prepareSubmission(
                PrepareSubmissionRequest(
                    assignmentId = assignmentId,
                    selectedDocumentId = selectedDocumentId
                )
            )
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Failed to prepare submission: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Submission preparation failed"))
        }
    }

    /**
     * Executes the submission ONLY when explicit student confirmation command is provided.
     * Validates idempotency token to prevent accidental duplicate submissions.
     */
    fun confirmAndSubmit(
        submissionId: String,
        idempotencyToken: String,
        studentConfirmationCommand: String
    ): Flow<NetworkResult<SubmissionResultResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.confirmAndSubmit(
                ConfirmSubmissionRequest(
                    submissionId = submissionId,
                    idempotencyToken = idempotencyToken,
                    studentConfirmationCommand = studentConfirmationCommand
                )
            )
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error("Submission authorization rejected: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Submission execution failed"))
        }
    }
}
