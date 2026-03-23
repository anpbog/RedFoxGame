package com.redfox.game.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.redfox.game.ui.screens.auth.LoginScreen
import com.redfox.game.ui.screens.auth.RegisterScreen
import com.redfox.game.ui.screens.auth.ResetPasswordScreen
import com.redfox.game.ui.screens.deposit.DepositScreen
import com.redfox.game.ui.screens.game.GameScreen
import com.redfox.game.ui.screens.main.MainScreen
import com.redfox.game.ui.screens.profile.ProfileScreen
import com.redfox.game.ui.screens.support.SupportScreen
import com.redfox.game.ui.screens.withdraw.WithdrawScreen

@Composable
fun RedFoxNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        // Демо-версия: стартуем с главного экрана (авторизация в Части 2)
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToResetPassword = { navController.navigate(Screen.ResetPassword.route) },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToGame = { mode ->
                    navController.navigate(Screen.Game.createRoute(mode))
                },
                onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "demo"
            GameScreen(
                mode = mode,
                onNavigateToDeposit = { navController.navigate(Screen.Deposit.route) },
                onNavigateToWithdraw = { navController.navigate(Screen.Withdraw.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Deposit.route) {
            DepositScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Withdraw.route) {
            WithdrawScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Support.route) {
            SupportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
