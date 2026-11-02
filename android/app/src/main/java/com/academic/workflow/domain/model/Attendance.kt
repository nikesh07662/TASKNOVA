package com.academic.workflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRecord(
    val subjectId: String,
    val subjectName: String,
    val totalLectures: Int,
    val attendedLectures: Int,
    val missedLectures: Int,
    val targetPercentage: Float = 75f,
    val lastUpdated: String = ""
) {
    val attendancePercentage: Float
        get() = if (totalLectures > 0) (attendedLectures.toFloat() / totalLectures.toFloat()) * 100f else 100f

    val isShortage: Boolean
        get() = attendancePercentage < targetPercentage

    val lecturesNeededForTarget: Int
        get() {
            if (attendancePercentage >= targetPercentage) return 0
            // Solve: (attended + X) / (total + X) >= target/100
            val target = targetPercentage / 100f
            val needed = ((target * totalLectures - attendedLectures) / (1f - target))
            return kotlin.math.ceil(needed.coerceAtLeast(0f)).toInt()
        }

    val lecturesCanMiss: Int
        get() {
            if (attendancePercentage <= targetPercentage) return 0
            val target = targetPercentage / 100f
            val canMiss = ((attendedLectures - target * totalLectures) / target)
            return kotlin.math.floor(canMiss.coerceAtLeast(0f)).toInt()
        }
}

@Serializable
data class AttendanceSummary(
    val overallPercentage: Float,
    val subjectsShortageCount: Int,
    val totalLecturesOverall: Int,
    val totalAttendedOverall: Int,
    val records: List<AttendanceRecord> = emptyList()
)
