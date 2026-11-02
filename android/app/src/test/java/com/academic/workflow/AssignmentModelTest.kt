package com.academic.workflow

import com.academic.workflow.domain.model.Assignment
import com.academic.workflow.domain.model.AssignmentStatus
import com.academic.workflow.domain.model.PlatformSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssignmentModelTest {

    @Test
    fun testAssignmentProperties() {
        val assignment = Assignment(
            id = "asgn_dbms_1",
            title = "Practical Lab 4: Relational Algebra & SQL Views",
            subject = "Database Management Systems",
            instructions = "Implement BCNF normal form tables.",
            deadline = "2026-11-10T23:59:00Z",
            platform = PlatformSource.GOOGLE_CLASSROOM,
            status = AssignmentStatus.PREPARED,
            matchedDocumentId = "doc_dbms_1",
            matchedDocumentName = "DBMS_Practical_Guide.pdf",
            isReadinessChecked = true
        )

        assertEquals("asgn_dbms_1", assignment.id)
        assertEquals(PlatformSource.GOOGLE_CLASSROOM, assignment.platform)
        assertEquals(AssignmentStatus.PREPARED, assignment.status)
        assertTrue(assignment.isReadinessChecked)
        assertFalse(assignment.isDuplicate)
        assertEquals("doc_dbms_1", assignment.matchedDocumentId)
    }

    @Test
    fun testMissingFilesWarning() {
        val assignment = Assignment(
            id = "asgn_os_1",
            title = "OS Milestone 2",
            subject = "Operating Systems",
            instructions = "POSIX Semaphores.",
            deadline = "2026-11-20T23:59:00Z",
            platform = PlatformSource.GOOGLE_CLASSROOM,
            status = AssignmentStatus.PENDING,
            missingFilesWarning = "No matching implementation file found in Drive."
        )

        assertEquals(AssignmentStatus.PENDING, assignment.status)
        assertTrue(assignment.missingFilesWarning != null)
    }
}
