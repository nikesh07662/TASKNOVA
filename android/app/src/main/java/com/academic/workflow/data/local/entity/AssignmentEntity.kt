package com.academic.workflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.academic.workflow.domain.model.Assignment
import com.academic.workflow.domain.model.AssignmentStatus
import com.academic.workflow.domain.model.PlatformSource

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val instructions: String,
    val deadline: String,
    val platform: String,
    val status: String,
    val sourcePlatformId: String?,
    val matchedDocumentId: String?,
    val matchedDocumentName: String?,
    val isDuplicate: Boolean,
    val isReadinessChecked: Boolean,
    val missingFilesWarning: String?
) {
    fun toDomain(): Assignment = Assignment(
        id = id,
        title = title,
        subject = subject,
        instructions = instructions,
        deadline = deadline,
        platform = PlatformSource.valueOf(platform),
        status = AssignmentStatus.valueOf(status),
        sourcePlatformId = sourcePlatformId,
        matchedDocumentId = matchedDocumentId,
        matchedDocumentName = matchedDocumentName,
        isDuplicate = isDuplicate,
        isReadinessChecked = isReadinessChecked,
        missingFilesWarning = missingFilesWarning
    )

    companion object {
        fun fromDomain(domain: Assignment): AssignmentEntity = AssignmentEntity(
            id = domain.id,
            title = domain.title,
            subject = domain.subject,
            instructions = domain.instructions,
            deadline = domain.deadline,
            platform = domain.platform.name,
            status = domain.status.name,
            sourcePlatformId = domain.sourcePlatformId,
            matchedDocumentId = domain.matchedDocumentId,
            matchedDocumentName = domain.matchedDocumentName,
            isDuplicate = domain.isDuplicate,
            isReadinessChecked = domain.isReadinessChecked,
            missingFilesWarning = domain.missingFilesWarning
        )
    }
}
