package com.academic.workflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.academic.workflow.data.local.entity.TaskMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskMemoryDao {
    @Query("SELECT * FROM task_memory ORDER BY updatedAt DESC")
    fun getAllMemoryItems(): Flow<List<TaskMemoryEntity>>

    @Query("SELECT * FROM task_memory WHERE category = :category ORDER BY updatedAt DESC")
    fun getMemoryByCategory(category: String): Flow<List<TaskMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TaskMemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TaskMemoryEntity>)

    @Query("DELETE FROM task_memory WHERE id = :id")
    suspend fun deleteById(id: String)
}
