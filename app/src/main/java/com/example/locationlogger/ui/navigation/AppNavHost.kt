package com.example.locationlogger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.locationlogger.ui.home.HomeScreen
import com.example.locationlogger.ui.timeline.TimelineScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {

        composable<HomeRoute> {
            HomeScreen(
                onNavigateToTimeline = dropUnlessResumed { navController.navigate(TimelineRoute) }
            )
        }
        composable<TimelineRoute> {
            TimelineScreen(
                onNavigateBack = dropUnlessResumed { navController.popBackStack() }
            )
        }
    }
}
