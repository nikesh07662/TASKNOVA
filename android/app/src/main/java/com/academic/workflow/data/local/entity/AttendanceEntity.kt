package com.academic.workflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.academic.workflow.domain.model.AttendanceRecord

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val subjectId: String,
    val subjectName: String,
    val totalLectures: Int,
    val attendedLectures: Int,
    val missedLectures: Int,
    val targetPercentage: Float,
    val lastUpdated: String
) {
    fun toDomain(): AttendanceRecord = AttendanceRecord(
        subjectId = subjectId,
        subjectName = subjectName,
        totalLectures = totalLectures,
        attendedLectures = attendedLectures,
        missedLectures = missedLectures,
        targetPercentage = targetPercentage,
        lastUpdated = lastUpdated
    )

    companion object {
        fun fromDomain(domain: AttendanceRecord): AttendanceEntity = AttendanceEntity(
            subjectId = domain.subjectId,
            subjectName = domain.subjectName,
            totalLectures = domain.totalLectures,
            attendedLectures = domain.attendedLectures,
            missedLectures = domain.missedLectures,
            targetPercentage = domain.targetPercentage,
            lastUpdated = domain.lastUpdated
        )
    }
}
