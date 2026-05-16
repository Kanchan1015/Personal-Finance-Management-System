package com.example.pbd.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pbd.ui.screens.auth.LoginScreen
import com.example.pbd.ui.screens.auth.RegisterScreen
import com.example.pbd.ui.screens.dashboard.DashboardScreen
import com.example.pbd.ui.screens.goal.GoalDetailScreen
import com.example.pbd.ui.screens.income.AddIncomeScreen
import com.example.pbd.ui.screens.expense.AddExpenseScreen
import com.example.pbd.ui.screens.profile.ProfileScreen
import com.example.pbd.ui.screens.transactions.TransactionHistoryScreen
import com.group.pbd.navigation.Screen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(navController = navController)
        }
        composable(
            route = Screen.GoalDetail.route,
            arguments = listOf(
                navArgument("goalId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId") ?: ""
            GoalDetailScreen(navController = navController, goalId = goalId)
        }
        composable(Screen.AddIncome.route) {
            AddIncomeScreen(navController = navController)
        }
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(navController = navController)
        }
    }
}