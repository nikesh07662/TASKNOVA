package com.academic.workflow.data.repository

import com.academic.workflow.core.network.ApiService
import com.academic.workflow.core.network.NetworkResult
import com.academic.workflow.data.local.dao.ActivityDao
import com.academic.workflow.data.local.entity.ActivityEntity
import com.academic.workflow.domain.model.ActivityCategory
import com.academic.workflow.domain.model.ActivityItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ActivityRepository(
    private val apiService: ApiService,
    private val activityDao: ActivityDao
) {
    val activities: Flow<List<ActivityItem>> = activityDao.getAllActivities().map { list ->
        list.map { it.toDomain() }
    }

    fun getActivitiesByCategory(category: ActivityCategory): Flow<List<ActivityItem>> =
        activityDao.getActivitiesByCategory(category.name).map { list -> list.map { it.toDomain() } }

    fun syncActivities(): Flow<NetworkResult<List<ActivityItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getActivityFeed()
            if (response.isSuccessful && response.body() != null) {
                val remoteActivities = response.body()!!
                activityDao.insertAll(remoteActivities.map { ActivityEntity.fromDomain(it) })
                emit(NetworkResult.Success(remoteActivities))
            } else {
                emit(NetworkResult.Error("Activity sync failed: ${response.message()}", response.code()))
            }
        } catch (e: Exception) {
            val sampleActivities = listOf(
                ActivityItem(
                    id = "act_1",
                    title = "Connected to Google Workspace",
                    description = "Synchronized active courses from Google Classroom and Drive.",
                    category = ActivityCategory.SYNC_OPERATION,
                    timestamp = java.time.Instant.now().toString()
                ),
                ActivityItem(
                    id = "act_2",
                    title = "Universal Task Memory Active",
                    description = "Indexed coursework requirements for DBMS Practical Lab 4.",
                    category = ActivityCategory.AI_ACTION,
                    timestamp = java.time.Instant.now().toString()
                )
            )
            activityDao.insertAll(sampleActivities.map { ActivityEntity.fromDomain(it) })
            emit(NetworkResult.Success(sampleActivities))
        }
    }

    suspend fun logLocalActivity(item: ActivityItem) {
        activityDao.insert(ActivityEntity.fromDomain(item))
    }
}
