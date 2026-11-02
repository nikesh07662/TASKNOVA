package com.academic.workflow.presentation.cloud

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.academic.workflow.core.theme.GoogleDriveYellow
import com.academic.workflow.core.theme.OneDriveBlue
import com.academic.workflow.core.theme.PrimaryBlue
import com.academic.workflow.core.theme.SecondaryTeal
import com.academic.workflow.domain.model.CloudStorageProvider
import com.academic.workflow.presentation.components.AcademicCard
import com.academic.workflow.presentation.components.AnimatedEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudScreen(viewModel: CloudViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showGenerateDialog by remember { mutableStateOf(false) }

    val tabTitles = listOf("All Cloud Files", "Google Drive", "OneDrive")
    var selectedTabIndex by remember { mutableStateOf(0) }

    val filteredFiles = when (selectedTabIndex) {
        1 -> uiState.files.filter { it.provider == CloudStorageProvider.GOOGLE_DRIVE }
        2 -> uiState.files.filter { it.provider == CloudStorageProvider.MICROSOFT_ONEDRIVE }
        else -> uiState.files
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cloud Document Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.syncFiles() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Sync Cloud Files")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showGenerateDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Doc Gen", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }

            // Notification / Success Banner
            if (uiState.generatedDocMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(SecondaryTeal.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.generatedDocMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryTeal
                    )
                }
            }

            // File List
            if (filteredFiles.isEmpty()) {
                AnimatedEmptyState(
                    icon = Icons.Filled.Folder,
                    title = "No Cloud Documents Found",
                    message = "Connect your Google Drive or Microsoft OneDrive in Settings, or generate documents using AI."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredFiles) { file ->
                        val providerColor = when (file.provider) {
                            CloudStorageProvider.GOOGLE_DRIVE -> GoogleDriveYellow
                            CloudStorageProvider.MICROSOFT_ONEDRIVE -> OneDriveBlue
                            CloudStorageProvider.LOCAL_STORAGE -> PrimaryBlue
                        }
                        val providerLabel = when (file.provider) {
                            CloudStorageProvider.GOOGLE_DRIVE -> "Drive"
                            CloudStorageProvider.MICROSOFT_ONEDRIVE -> "OneDrive"
                            CloudStorageProvider.LOCAL_STORAGE -> "Local"
                        }

                        AcademicCard(
                            title = file.name,
                            subtitle = "${file.path} • Modified: ${file.modifiedTime.take(10)}",
                            badgeContent = {
                                Box(
                                    modifier = Modifier
                                        .background(providerColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = providerLabel,
                                        color = providerColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${file.sizeBytes / 1024} KB",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (file.isGeneratedDoc) {
                                    Text(
                                        text = "AI Generated",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Document Generation Dialog
    if (showGenerateDialog) {
        var docTitle by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("") }
        var instructions by remember { mutableStateOf("") }
        var docType by remember { mutableStateOf("ASSIGNMENT") }

        AlertDialog(
            onDismissRequest = { showGenerateDialog = false },
            title = {
                Text(
                    text = "AI Document Generator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Generate structured assignment or practical code and save directly to Cloud.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = docTitle,
                        onValueChange = { docTitle = it },
                        label = { Text("Document Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (e.g. DBMS, OS)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Prompt & Instructions") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docTitle.isNotBlank() && subject.isNotBlank()) {
                            viewModel.generateAcademicDocument(
                                title = docTitle,
                                subject = subject,
                                type = docType,
                                instructions = instructions
                            )
                            showGenerateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (uiState.isGeneratingDoc) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Generate & Save")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showGenerateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
