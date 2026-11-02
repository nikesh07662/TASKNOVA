package com.academic.workflow

import com.academic.workflow.core.network.IntegrationDashboardResponse
import com.academic.workflow.core.network.IntegrationHealthItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IntegrationHealthTest {

    @Test
    fun testIntegrationDashboardCounts() {
        val items = listOf(
            IntegrationHealthItem(
                id = "google_classroom",
                name = "Google Classroom",
                category = "GOOGLE",
                status = "WORKING",
                statusDisplay = "Operational",
                isConnected = true,
                requiresAdminConsent = true,
                adminConsentNote = "Admin consent required."
            ),
            IntegrationHealthItem(
                id = "openai_api",
                name = "OpenAI API",
                category = "AI",
                status = "CONFIGURATION_INCOMPLETE",
                statusDisplay = "Key Pending",
                isConnected = false,
                requiresAdminConsent = false
            )
        )

        val dashboard = IntegrationDashboardResponse(
            totalIntegrations = items.size,
            activeConnectedCount = items.count { it.isConnected },
            needsAttentionCount = items.count { !it.isConnected },
            items = items
        )

        assertEquals(2, dashboard.totalIntegrations)
        assertEquals(1, dashboard.activeConnectedCount)
        assertEquals(1, dashboard.needsAttentionCount)
        assertTrue(dashboard.items[0].requiresAdminConsent)
        assertFalse(dashboard.items[1].requiresAdminConsent)
    }

    @Test
    fun testCategoryClassification() {
        val google = IntegrationHealthItem(
            id = "google_drive",
            name = "Google Drive",
            category = "GOOGLE",
            status = "WORKING",
            isConnected = true
        )
        val ms = IntegrationHealthItem(
            id = "microsoft_teams",
            name = "Microsoft Teams",
            category = "MICROSOFT",
            status = "WORKING",
            isConnected = true
        )

        assertEquals("GOOGLE", google.category)
        assertEquals("MICROSOFT", ms.category)
    }
}
