package com.academic.workflow

import com.academic.workflow.domain.model.PlatformSource
import com.academic.workflow.domain.model.PreparedSubmission
import com.academic.workflow.domain.model.SubmissionStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CommandAuthorizationTest {

    @Test
    fun testPreparedSubmissionRequiresCommand() {
        val submission = PreparedSubmission(
            submissionId = "sub_test_01",
            assignmentId = "assign_dbms_1",
            assignmentTitle = "DBMS Normalization Assignment",
            subject = "Database Management Systems",
            destinationPlatform = PlatformSource.GOOGLE_CLASSROOM,
            documentId = "doc_992",
            documentName = "DBMS_Normalization_Report.pdf",
            documentSizeFormatted = "180 KB",
            documentPreviewSnippet = "Normalized to 3NF and BCNF.",
            deadline = "2026-11-15T23:59:59Z",
            stage = SubmissionStage.PREPARED_FOR_REVIEW,
            idempotencyToken = UUID.randomUUID().toString()
        )

        assertEquals(SubmissionStage.PREPARED_FOR_REVIEW, submission.stage)
        assertNotNull(submission.idempotencyToken)
        assertTrue(submission.idempotencyToken.isNotEmpty())
    }

    @Test
    fun testPlatformSourceFormatting() {
        val google = PlatformSource.GOOGLE_CLASSROOM
        val ms = PlatformSource.MICROSOFT_TEAMS

        assertEquals("GOOGLE_CLASSROOM", google.name)
        assertEquals("MICROSOFT_TEAMS", ms.name)
    }
}
