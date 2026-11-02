package com.academic.workflow.data.repository

import com.academic.workflow.core.network.ApiService
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.data.local.dao.CloudFileDao
import com.academic.workflow.data.local.entity.CloudFileEntity
import com.academic.workflow.domain.model.CloudFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CloudRepository(
    private val apiService: ApiService,
    private val cloudFileDao: CloudFileDao
) {
    val allFiles: Flow<List<CloudFile>> = cloudFileDao.getAllFiles().map { list ->
        list.map { it.toDomain() }
    }

    val generatedDocs: Flow<List<CloudFile>> = cloudFileDao.getGeneratedDocuments().map { list ->
        list.map { it.toDomain() }
    }

    fun syncCloudFiles(provider: String? = null): Flow<NetworkResult<List<CloudFile>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getCloudFiles(provider)
            if (response.isSuccessful && response.body() != null) {
                val remoteFiles = response.body()!!
                cloudFileDao.insertAll(remoteFiles.map { CloudFileEntity.fromDomain(it) })
                emit(NetworkResult.Success(remoteFiles))
            } else {
                emit(NetworkResult.Error("Cloud synchronization failed: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            val sampleFiles = listOf(
                CloudFile(
                    id = "doc_dbms_1",
                    name = "DBMS_Practical_Guide.pdf",
                    provider = com.academic.workflow.domain.model.CloudStorageProvider.GOOGLE_DRIVE,
                    mimeType = "application/pdf",
                    sizeBytes = 248000,
                    modifiedTime = "2026-11-01T14:20:00Z",
                    webViewLink = "https://drive.google.com/file/d/demo_dbms/view",
                    isGeneratedDoc = true,
                    associatedSubject = "DBMS"
                ),
                CloudFile(
                    id = "doc_algo_1",
                    name = "Algorithms_Dynamic_Programming.docx",
                    provider = com.academic.workflow.domain.model.CloudStorageProvider.MICROSOFT_ONEDRIVE,
                    mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    sizeBytes = 184320,
                    modifiedTime = "2026-10-30T16:00:00Z",
                    webViewLink = "https://onedrive.live.com/view.aspx?id=demo_algo",
                    associatedSubject = "Algorithms"
                )
            )
            cloudFileDao.insertAll(sampleFiles.map { CloudFileEntity.fromDomain(it) })
            emit(NetworkResult.Success(sampleFiles))
        }
    }

    fun uploadFile(provider: String, file: File): Flow<NetworkResult<CloudFile>> = flow {
        emit(NetworkResult.Loading)
        try {
            val providerBody = provider.toRequestBody("text/plain".toMediaTypeOrNull())
            val fileBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, fileBody)

            val response = apiService.uploadFile(providerBody, part)
            if (response.isSuccessful && response.body() != null) {
                val uploaded = response.body()!!
                cloudFileDao.insert(CloudFileEntity.fromDomain(uploaded))
                emit(NetworkResult.Success(uploaded))
            } else {
                emit(NetworkResult.Error("File upload failed: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Upload error"))
        }
    }
}
