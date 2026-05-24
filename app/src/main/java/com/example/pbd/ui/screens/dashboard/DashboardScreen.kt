package com.example.pbd.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavHostController
import com.example.pbd.ui.screens.dashboard.components.*
import com.example.pbd.ui.theme.*
import com.example.pbd.navigation.Screen

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentPurple)
        }
        return
    }

    val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Fixed Toolbar Header ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = statusBarHeightDp + 16.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkCard)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Analytics",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Financial Insights",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Scrollable Content Area ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
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
                progress = uiState.goalProgress,
                onBoost = { amount -> viewModel.boostActiveGoal(amount) },
                onClick = { navController.navigate(Screen.GoalDetail.createRoute(goal.id)) }
            )
        }

        // Income overview
        if (uiState.incomeBreakdown.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Income Overview",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { navController.navigate(Screen.TransactionHistory.createRoute("income")) }) {
                    Text(text = "Details", color = AccentPurple, fontSize = 12.sp)
                }
            }
            IncomeOverview(incomeBreakdown = uiState.incomeBreakdown)
        }

        // Spending overview
        if (uiState.categoryBreakdown.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending Overview",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { navController.navigate(Screen.TransactionHistory.createRoute("expense")) }) {
                    Text(text = "Details", color = AccentPurple, fontSize = 12.sp)
                }
            }
            SpendingOverview(categoryBreakdown = uiState.categoryBreakdown)
        }

        // Top spending
        if (uiState.categoryBreakdown.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Spending",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { navController.navigate(Screen.TransactionHistory.createRoute("expense")) }) {
                    Text(text = "View All", color = AccentPurple, fontSize = 12.sp)
                }
            }
            TopSpending(categoryBreakdown = uiState.categoryBreakdown)
        }

        // Recent transactions
        if (uiState.recentTransactions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { navController.navigate(Screen.TransactionHistory.createRoute("all")) }) {
                    Text(text = "See All", color = AccentPurple, fontSize = 12.sp)
                }
            }
            RecentTransactions(transactions = uiState.recentTransactions)
        }

        Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}