package com.group.pbd.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")

    object AddIncome : Screen("add_income")

    object AddExpense : Screen("add_expense")

    object Dashboard : Screen("dashboard")
    object TransactionHistory : Screen("transaction_history")
    object GoalDetail : Screen("goal_detail/{goalId}") {
        fun createRoute(goalId: String) = "goal_detail/$goalId"
    }
}