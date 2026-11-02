package com.academic.workflow.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.academic.workflow.AcademicFlowApplication
import com.academic.workflow.core.theme.AcademicFlowTheme
import com.academic.workflow.presentation.navigation.AcademicNavGraph
import com.academic.workflow.presentation.navigation.BottomNavItems
import com.academic.workflow.presentation.navigation.Screen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as AcademicFlowApplication
        val container = app.container

        setContent {
            val isDarkThemePref by container.preferencesManager.isDarkMode.collectAsState(
                initial = isSystemInDarkTheme()
            )

            AcademicFlowTheme(darkTheme = isDarkThemePref) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AcademicAppRoot(container = container)
                }
            }
        }

        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIncomingIntent(it) }
    }

    private fun handleIncomingIntent(intent: Intent) {
        val uri = intent.data
        if (uri != null && uri.scheme == "academicflow" && uri.host == "oauth-callback") {
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            // Handle OAuth callback securely
        }
    }
}

@Composable
fun AcademicAppRoot(container: com.academic.workflow.core.di.AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = BottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(tonalElevation = 4.dp) {
                    BottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                val iconVector = if (selected) screen.selectedIcon else screen.unselectedIcon
                                if (iconVector != null) {
                                    Icon(imageVector = iconVector, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AcademicNavGraph(
                navController = navController,
                container = container
            )
        }
    }
}
