package com.academic.workflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.academic.workflow.domain.model.ActivityCategory
import com.academic.workflow.domain.model.ActivityItem

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val timestamp: String,
    val isSuccess: Boolean,
    val relatedEntityId: String?
) {
    fun toDomain(): ActivityItem = ActivityItem(
        id = id,
        title = title,
        description = description,
        category = ActivityCategory.valueOf(category),
        timestamp = timestamp,
        isSuccess = isSuccess,
        relatedEntityId = relatedEntityId
    )

    companion object {
        fun fromDomain(domain: ActivityItem): ActivityEntity = ActivityEntity(
            id = domain.id,
            title = domain.title,
            description = domain.description,
            category = domain.category.name,
            timestamp = domain.timestamp,
            isSuccess = domain.isSuccess,
            relatedEntityId = domain.relatedEntityId
        )
    }
}
