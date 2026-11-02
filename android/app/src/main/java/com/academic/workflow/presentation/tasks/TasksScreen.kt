package com.academic.workflow.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.academic.workflow.core.theme.ErrorRed
import com.academic.workflow.core.theme.PrimaryBlue
import com.academic.workflow.domain.model.AssignmentStatus
import com.academic.workflow.presentation.components.AcademicCard
import com.academic.workflow.presentation.components.AnimatedEmptyState
import com.academic.workflow.presentation.components.PlatformBadge
import com.academic.workflow.presentation.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    onNavigateToSubmission: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Assignments & Deadlines",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (uiState.isCalendarView) Icons.Filled.FormatListBulleted else Icons.Filled.CalendarMonth,
                            contentDescription = "Toggle View Mode"
                        )
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
            // Search field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search by subject or title...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == TaskFilter.ALL,
                        onClick = { viewModel.setFilter(TaskFilter.ALL) },
                        label = { Text("All (${uiState.assignments.size})", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == TaskFilter.PENDING,
                        onClick = { viewModel.setFilter(TaskFilter.PENDING) },
                        label = { Text("Pending", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == TaskFilter.READY_TO_SUBMIT,
                        onClick = { viewModel.setFilter(TaskFilter.READY_TO_SUBMIT) },
                        label = { Text("Ready to Submit", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == TaskFilter.SUBMITTED,
                        onClick = { viewModel.setFilter(TaskFilter.SUBMITTED) },
                        label = { Text("Submitted", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.selectedFilter == TaskFilter.OVERDUE,
                        onClick = { viewModel.setFilter(TaskFilter.OVERDUE) },
                        label = { Text("Overdue", fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assignments List
            if (uiState.filteredAssignments.isEmpty()) {
                AnimatedEmptyState(
                    icon = Icons.Filled.CheckCircle,
                    title = "No Tasks Found",
                    message = "No assignments match the selected filter."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredAssignments) { assignment ->
                        AcademicCard(
                            title = assignment.title,
                            subtitle = "${assignment.subject} • Due: ${assignment.deadline.take(10)}",
                            badgeContent = {
                                StatusBadge(status = assignment.status)
                            },
                            expandable = true
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PlatformBadge(platform = assignment.platform)

                                    if (assignment.isReadinessChecked) {
                                        Text(
                                            text = "✓ Document Verified",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Instructions: ${assignment.instructions}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (assignment.matchedDocumentName != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Description,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Matched File: ${assignment.matchedDocumentName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                if (!assignment.missingFilesWarning.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚠️ ${assignment.missingFilesWarning}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ErrorRed
                                    )
                                }

                                if (assignment.status != AssignmentStatus.SUBMITTED) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onNavigateToSubmission(assignment.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Authorize Submission Flow")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
