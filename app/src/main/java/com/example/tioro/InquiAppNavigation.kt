package com.example.tioro

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

@Composable
fun InquiAppNavigation(viewModel: InquiViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.isAppLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFE5C07B))
        }
        return
    }

    val startDestination = if (uiState.userName.isNullOrBlank()) "welcome" else "home"
    val navController = rememberNavController()

    NavHost(
        navController = navController, 
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        
        composable("welcome") {
            WelcomeScreen(
                onSaveName = { name ->
                    viewModel.saveUserName(name)
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "home",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            InquiHomeScreen(
                navController = navController,
                viewModel = viewModel,
                onNavigateToRegister = { activityType ->
                    navController.navigate("register/$activityType")
                },
                onNavigateToDailyList = {
                    navController.navigate("full_daily_list")
                },
                onNavigateToNewProject = {
                    navController.navigate("new_project")
                }
            )
        }

        composable(
            "full_daily_list",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(250)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(tween(200)) }
        ) {
            FullDailyListScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = "register/{activityType}",
            arguments = listOf(navArgument("activityType") { type = NavType.StringType }),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(250)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(tween(200)) }
        ) { backStackEntry ->
            val activityType = backStackEntry.arguments?.getString("activityType") ?: "Actividad"
            
            RegistrarActividadScreen(
                navController = navController,
                viewModel = viewModel,
                initialActivityType = activityType
            )
        }
        
        composable(
            "new_project",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(250)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(tween(200)) }
        ) {
            NewProjectScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            "weekly_stats",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            WeeklyStatsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            "monthly_stats",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(250)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(tween(200)) }
        ) {
            MonthlyStatsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            "yearly_stats",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(250)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(tween(200)) }
        ) {
            AnnualStatsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            "settings",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            SettingsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            "backup_screen",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(250)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(tween(200)) }
        ) {
            BackupScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            "notifications_screen",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(250)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(tween(200)) }
        ) {
            NotificationsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            "success_activity",
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            ConfirmationScreen(
                navController = navController,
                title = "¡Excelente!",
                subtitle = "Actividad registrada",
                accentColor = Color(0xFFE5C07B)
            )
        }

        composable(
            "success_project",
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            ConfirmationScreen(
                navController = navController,
                title = "¡Excelente!",
                subtitle = "Proyecto registrado",
                accentColor = Color(0xFFE5C07B)
            )
        }
    }
}
