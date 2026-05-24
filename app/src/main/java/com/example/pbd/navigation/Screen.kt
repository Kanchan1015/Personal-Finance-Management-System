package com.example.pbd.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object AddIncome : Screen("add_income")
    object AddExpense : Screen("add_expense")
    object Dashboard : Screen("dashboard")
    object TransactionHistory : Screen("transaction_history/{mode}") {
        fun createRoute(mode: String) = "transaction_history/$mode"
    }
    object GoalDetail : Screen("goal_detail/{goalId}") {
        fun createRoute(goalId: String) = "goal_detail/$goalId"
    }
}
