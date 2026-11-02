package com.academic.workflow.presentation.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.academic.workflow.core.theme.PrimaryBlue
import com.academic.workflow.core.theme.SecondaryTeal
import com.academic.workflow.core.theme.SuccessGreen
import com.academic.workflow.core.theme.WarningAmber
import com.academic.workflow.domain.model.ActivityCategory
import com.academic.workflow.domain.model.ActivityItem
import com.academic.workflow.presentation.components.AcademicCard
import com.academic.workflow.presentation.components.AnimatedEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(viewModel: ActivityViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val categories = listOf(
        null to "All",
        ActivityCategory.SUBMISSION_AUDIT to "Submissions",
        ActivityCategory.AI_ACTION to "AI Operations",
        ActivityCategory.DOCUMENT_GENERATION to "Documents",
        ActivityCategory.SYNC_OPERATION to "Sync",
        ActivityCategory.NOTIFICATION to "Alerts"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Activity & Audit Log",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshActivities() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh Activity Feed")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (cat, label) ->
                    val isSelected = uiState.selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.activities.isEmpty()) {
                AnimatedEmptyState(
                    icon = Icons.Filled.History,
                    title = "No Activity Logged",
                    message = "Actions, AI document generations, and verified platform submissions will appear here."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.activities) { item ->
                        ActivityRowItem(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityRowItem(item: ActivityItem) {
    val (icon, tintColor) = when (item.category) {
        ActivityCategory.SUBMISSION_AUDIT -> Pair(Icons.Filled.Check, SuccessGreen)
        ActivityCategory.AI_ACTION -> Pair(Icons.Filled.AutoAwesome, PrimaryBlue)
        ActivityCategory.DOCUMENT_GENERATION -> Pair(Icons.Filled.Description, SecondaryTeal)
        ActivityCategory.SYNC_OPERATION -> Pair(Icons.Filled.Sync, WarningAmber)
        ActivityCategory.NOTIFICATION -> Pair(Icons.Filled.Notifications, PrimaryBlue)
        ActivityCategory.ATTENDANCE_UPDATE -> Pair(Icons.Filled.History, SecondaryTeal)
    }

    AcademicCard(
        title = item.title,
        subtitle = item.timestamp.take(19).replace('T', ' ')
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(tintColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
