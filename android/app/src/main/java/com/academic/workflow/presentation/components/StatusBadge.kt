package com.academic.workflow.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.academic.workflow.core.theme.ErrorRed
import com.academic.workflow.core.theme.PrimaryBlue
import com.academic.workflow.core.theme.SecondaryTeal
import com.academic.workflow.core.theme.SuccessGreen
import com.academic.workflow.core.theme.WarningAmber
import com.academic.workflow.domain.model.AssignmentStatus
import com.academic.workflow.domain.model.PlatformSource

@Composable
fun StatusBadge(
    status: AssignmentStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        AssignmentStatus.PENDING -> Triple(WarningAmber.copy(alpha = 0.15f), WarningAmber, "Pending")
        AssignmentStatus.IN_PROGRESS -> Triple(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue, "In Progress")
        AssignmentStatus.PREPARED -> Triple(SecondaryTeal.copy(alpha = 0.15f), SecondaryTeal, "Ready to Submit")
        AssignmentStatus.SUBMITTED -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "Submitted")
        AssignmentStatus.OVERDUE -> Triple(ErrorRed.copy(alpha = 0.15f), ErrorRed, "Overdue")
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun PlatformBadge(
    platform: PlatformSource,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (platform) {
        PlatformSource.GOOGLE_CLASSROOM -> Triple(Color(0xFFE6F4EA), Color(0xFF137333), "Google Classroom")
        PlatformSource.MICROSOFT_TEAMS -> Triple(Color(0xFFEEF0FA), Color(0xFF464EB8), "MS Teams")
        PlatformSource.MANUAL -> Triple(Color(0xFFF1F3F4), Color(0xFF5F6368), "Custom")
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
