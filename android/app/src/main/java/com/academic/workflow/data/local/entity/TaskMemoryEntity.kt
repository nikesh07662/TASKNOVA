package com.academic.workflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_memory")
data class TaskMemoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val category: String, // "ASSIGNMENT", "DOCUMENT", "CONVERSATION", "DEADLINE"
    val relatedEntityId: String?,
    val metadataJson: String,
    val updatedAt: String
)
