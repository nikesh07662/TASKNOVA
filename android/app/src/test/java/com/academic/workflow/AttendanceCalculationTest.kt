package com.academic.workflow

import com.academic.workflow.domain.model.AttendanceRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttendanceCalculationTest {

    @Test
    fun testAttendancePercentageCalculation() {
        val record = AttendanceRecord(
            subjectId = "cs101",
            subjectName = "Database Management Systems",
            totalLectures = 40,
            attendedLectures = 32,
            missedLectures = 8,
            targetPercentage = 75f
        )

        assertEquals(80.0f, record.attendancePercentage, 0.01f)
        assertFalse(record.isShortage)
    }

    @Test
    fun testAttendanceShortageDetection() {
        val record = AttendanceRecord(
            subjectId = "cs102",
            subjectName = "Operating Systems",
            totalLectures = 20,
            attendedLectures = 14,
            missedLectures = 6,
            targetPercentage = 75f
        )

        assertEquals(70.0f, record.attendancePercentage, 0.01f)
        assertTrue(record.isShortage)
        assertEquals(4, record.lecturesNeededForTarget)
    }

    @Test
    fun testLecturesCanMissCalculation() {
        val record = AttendanceRecord(
            subjectId = "cs103",
            subjectName = "Computer Networks",
            totalLectures = 20,
            attendedLectures = 19,
            missedLectures = 1,
            targetPercentage = 75f
        )

        assertEquals(95.0f, record.attendancePercentage, 0.01f)
        assertFalse(record.isShortage)
        assertTrue(record.lecturesCanMiss >= 5)
    }
}
