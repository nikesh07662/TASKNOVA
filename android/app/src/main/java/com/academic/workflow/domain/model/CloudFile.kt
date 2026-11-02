package com.academic.workflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CloudStorageProvider {
    GOOGLE_DRIVE,
    MICROSOFT_ONEDRIVE,
    LOCAL_STORAGE
}

@Serializable
data class CloudFile(
    val id: String,
    val name: String,
    val provider: CloudStorageProvider,
    val mimeType: String,
    val sizeBytes: Long,
    val modifiedTime: String,
    val webViewLink: String? = null,
    val downloadUrl: String? = null,
    val path: String = "/",
    val isGeneratedDoc: Boolean = false,
    val associatedSubject: String? = null
)
