package com.redfox.game.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ResetPassword : Screen("reset_password")
    data object Main : Screen("main")
    data object Game : Screen("game/{mode}") {
        fun createRoute(mode: String) = "game/$mode"
    }
    data object Deposit : Screen("deposit")
    data object Withdraw : Screen("withdraw")
    data object Profile : Screen("profile")
    data object Support : Screen("support")
}
