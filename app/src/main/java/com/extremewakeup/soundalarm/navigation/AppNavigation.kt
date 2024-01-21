package com.extremewakeup.soundalarm.navigation

import AlarmScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.extremewakeup.soundalarm.ui.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "alarmScreen") {
        composable("alarmScreen") {
            AlarmScreen(navController, viewModel)
        }

    }
}
