package com.academic.workflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.academic.workflow.data.local.entity.CloudFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudFileDao {
    @Query("SELECT * FROM cloud_files ORDER BY modifiedTime DESC")
    fun getAllFiles(): Flow<List<CloudFileEntity>>

    @Query("SELECT * FROM cloud_files WHERE provider = :provider ORDER BY modifiedTime DESC")
    fun getFilesByProvider(provider: String): Flow<List<CloudFileEntity>>

    @Query("SELECT * FROM cloud_files WHERE isGeneratedDoc = 1 ORDER BY modifiedTime DESC")
    fun getGeneratedDocuments(): Flow<List<CloudFileEntity>>

    @Query("SELECT * FROM cloud_files WHERE id = :id")
    suspend fun getFileById(id: String): CloudFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<CloudFileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: CloudFileEntity)

    @Query("DELETE FROM cloud_files WHERE id = :id")
    suspend fun deleteById(id: String)
}
