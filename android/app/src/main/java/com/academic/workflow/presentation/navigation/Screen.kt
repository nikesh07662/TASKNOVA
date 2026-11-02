package com.academic.workflow.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Auth : Screen("auth", "Authentication")
    
    // Primary Bottom Navigation Destinations
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Assistant : Screen("assistant", "AI Assistant", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Tasks : Screen("tasks", "Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    object Cloud : Screen("cloud", "Cloud", Icons.Filled.Cloud, Icons.Outlined.Cloud)
    object Activity : Screen("activity", "Activity", Icons.Filled.History, Icons.Outlined.History)

    // Deep / Secondary Destinations
    object Settings : Screen("settings", "Settings & Integrations")
    object SubmissionConfirmation : Screen("submission/{submissionId}", "Confirm Submission") {
        fun createRoute(submissionId: String) = "submission/$submissionId"
    }
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Assistant,
    Screen.Tasks,
    Screen.Cloud,
    Screen.Activity
)
