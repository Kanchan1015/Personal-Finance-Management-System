package com.example.pbd.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavHostController
import com.example.pbd.ui.screens.dashboard.components.*
import com.example.pbd.ui.theme.*

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentPurple)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Balance card
        BalanceCard(
            totalBalance = uiState.netBalance,
            totalIncome = uiState.totalIncome,
            totalExpenses = uiState.totalExpenses
        )

        // Goal card
        uiState.activeGoal?.let { goal ->
            GoalCard(
                goal = goal,
                progress = uiState.goalProgress
            )
        }

        // Spending overview
        if (uiState.categoryBreakdown.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spending Overview",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { }) {
                    Text(text = "Details", color = AccentPurple, fontSize = 12.sp)
                }
            }
            SpendingOverview(categoryBreakdown = uiState.categoryBreakdown)
        }

        // Top spending
        if (uiState.categoryBreakdown.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Top Spending",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { }) {
                    Text(text = "View All", color = AccentPurple, fontSize = 12.sp)
                }
            }
            TopSpending(categoryBreakdown = uiState.categoryBreakdown)
        }

        // Recent transactions
        if (uiState.recentTransactions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recent Transactions",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { navController.navigate("transaction_history") }) {
                    Text(text = "See All", color = AccentPurple, fontSize = 12.sp)
                }
            }
            RecentTransactions(transactions = uiState.recentTransactions)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}