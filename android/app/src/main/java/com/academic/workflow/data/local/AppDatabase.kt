package com.academic.workflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.academic.workflow.data.local.dao.ActivityDao
import com.academic.workflow.data.local.dao.AssignmentDao
import com.academic.workflow.data.local.dao.AttendanceDao
import com.academic.workflow.data.local.dao.CloudFileDao
import com.academic.workflow.data.local.dao.TaskMemoryDao
import com.academic.workflow.data.local.entity.ActivityEntity
import com.academic.workflow.data.local.entity.AssignmentEntity
import com.academic.workflow.data.local.entity.AttendanceEntity
import com.academic.workflow.data.local.entity.CloudFileEntity
import com.academic.workflow.data.local.entity.TaskMemoryEntity

@Database(
    entities = [
        AssignmentEntity::class,
        AttendanceEntity::class,
        CloudFileEntity::class,
        ActivityEntity::class,
        TaskMemoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assignmentDao(): AssignmentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun cloudFileDao(): CloudFileDao
    abstract fun activityDao(): ActivityDao
    abstract fun taskMemoryDao(): TaskMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "academic_flow.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
