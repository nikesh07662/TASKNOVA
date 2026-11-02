package com.academic.workflow.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.academic.workflow.core.di.AppContainer
import com.academic.workflow.presentation.activity.ActivityScreen
import com.academic.workflow.presentation.activity.ActivityViewModel
import com.academic.workflow.presentation.assistant.AssistantScreen
import com.academic.workflow.presentation.assistant.AssistantViewModel
import com.academic.workflow.presentation.auth.AuthScreen
import com.academic.workflow.presentation.auth.AuthViewModel
import com.academic.workflow.presentation.cloud.CloudScreen
import com.academic.workflow.presentation.cloud.CloudViewModel
import com.academic.workflow.presentation.home.HomeScreen
import com.academic.workflow.presentation.home.HomeViewModel
import com.academic.workflow.presentation.settings.SettingsScreen
import com.academic.workflow.presentation.settings.SettingsViewModel
import com.academic.workflow.presentation.submission.SubmissionConfirmationScreen
import com.academic.workflow.presentation.submission.SubmissionViewModel
import com.academic.workflow.presentation.tasks.TasksScreen
import com.academic.workflow.presentation.tasks.TasksViewModel

@Composable
fun AcademicNavGraph(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val startDestination = if (container.authRepository.isAuthenticated()) {
        Screen.Home.route
    } else {
        Screen.Auth.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) }
    ) {
        composable(Screen.Auth.route) {
            val authViewModel = AuthViewModel(container.authRepository)
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel = HomeViewModel(
                container.assignmentRepository,
                container.attendanceRepository,
                container.activityRepository
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToAssistant = { navController.navigate(Screen.Assistant.route) },
                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                onNavigateToSubmission = { assignmentId ->
                    navController.navigate(Screen.SubmissionConfirmation.createRoute(assignmentId))
                }
            )
        }

        composable(Screen.Assistant.route) {
            val assistantViewModel = AssistantViewModel(
                container.aiRepository,
                container.preferencesManager
            )
            AssistantScreen(
                viewModel = assistantViewModel,
                onNavigateToSubmission = { assignmentId ->
                    navController.navigate(Screen.SubmissionConfirmation.createRoute(assignmentId))
                }
            )
        }

        composable(Screen.Tasks.route) {
            val tasksViewModel = TasksViewModel(container.assignmentRepository)
            TasksScreen(
                viewModel = tasksViewModel,
                onNavigateToSubmission = { assignmentId ->
                    navController.navigate(Screen.SubmissionConfirmation.createRoute(assignmentId))
                }
            )
        }

        composable(Screen.Cloud.route) {
            val cloudViewModel = CloudViewModel(container.cloudRepository, container.aiRepository)
            CloudScreen(viewModel = cloudViewModel)
        }

        composable(Screen.Activity.route) {
            val activityViewModel = ActivityViewModel(container.activityRepository)
            ActivityScreen(viewModel = activityViewModel)
        }

        composable(Screen.Settings.route) {
            val settingsViewModel = SettingsViewModel(
                container.preferencesManager,
                container.authRepository,
                container.integrationRepository
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.SubmissionConfirmation.route,
            arguments = listOf(navArgument("submissionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val submissionId = backStackEntry.arguments?.getString("submissionId") ?: ""
            val submissionViewModel = SubmissionViewModel(
                container.submissionRepository,
                container.assignmentRepository
            )
            SubmissionConfirmationScreen(
                assignmentId = submissionId,
                viewModel = submissionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
