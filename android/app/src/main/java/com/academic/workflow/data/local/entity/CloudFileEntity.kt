package com.academic.workflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.academic.workflow.domain.model.CloudFile
import com.academic.workflow.domain.model.CloudStorageProvider

@Entity(tableName = "cloud_files")
data class CloudFileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val provider: String,
    val mimeType: String,
    val sizeBytes: Long,
    val modifiedTime: String,
    val webViewLink: String?,
    val downloadUrl: String?,
    val path: String,
    val isGeneratedDoc: Boolean,
    val associatedSubject: String?
) {
    fun toDomain(): CloudFile = CloudFile(
        id = id,
        name = name,
        provider = CloudStorageProvider.valueOf(provider),
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        modifiedTime = modifiedTime,
        webViewLink = webViewLink,
        downloadUrl = downloadUrl,
        path = path,
        isGeneratedDoc = isGeneratedDoc,
        associatedSubject = associatedSubject
    )

    companion object {
        fun fromDomain(domain: CloudFile): CloudFileEntity = CloudFileEntity(
            id = domain.id,
            name = domain.name,
            provider = domain.provider.name,
            mimeType = domain.mimeType,
            sizeBytes = domain.sizeBytes,
            modifiedTime = domain.modifiedTime,
            webViewLink = domain.webViewLink,
            downloadUrl = domain.downloadUrl,
            path = domain.path,
            isGeneratedDoc = domain.isGeneratedDoc,
            associatedSubject = domain.associatedSubject
        )
    }
}
