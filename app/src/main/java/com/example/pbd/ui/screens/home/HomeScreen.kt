package com.example.pbd.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbd.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import com.example.pbd.ui.screens.dashboard.DashboardViewModel
import com.example.pbd.ui.screens.dashboard.components.GoalCard

import com.example.pbd.ui.screens.dashboard.components.SpendingOverview
import com.example.pbd.ui.screens.dashboard.components.IncomeOverview

// ── Colour palette ────────────────────────────────────────────────────────────
private val BgDark        = Color(0xFF0D0F1A)
private val CardDark      = Color(0xFF161929)
private val AccentPurple  = Color(0xFF7B61FF)
private val AccentBlue    = Color(0xFF4FC3F7)
private val AccentGreen   = Color(0xFF4CAF50)
private val AccentOrange  = Color(0xFFFF9800)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB0B8D0)

private val CardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF7B61FF), Color(0xFF4FC3F7))
)

// ── Main composable ───────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentPurple)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Spacer(Modifier.height(48.dp))

                // ── Top bar ──────────────────────────────────────────────────────
                TopBar(
                    userName = uiState.userName,
                    onAvatarClick = { navController.navigate(Screen.Profile.route) }
                )

                Spacer(Modifier.height(24.dp))

                // ── Balance card ─────────────────────────────────────────────────
                BalanceCard(
                    balance = uiState.netBalance,
                    income = uiState.totalIncome,
                    expenses = uiState.totalExpenses,
                    onCardClick = { navController.navigate(Screen.Dashboard.route) },
                    onIncomeClick = { navController.navigate(Screen.TransactionHistory.createRoute("income")) },
                    onExpensesClick = { navController.navigate(Screen.TransactionHistory.createRoute("expense")) }
                )

                Spacer(Modifier.height(24.dp))

                // ── Goal Card ───────────────────────────────────────────────────
                uiState.activeGoal?.let { goal ->
                    GoalCard(
                        goal = goal,
                        progress = uiState.goalProgress,
                        onBoost = { amount -> viewModel.boostActiveGoal(amount) },
                        onClick = { navController.navigate(Screen.GoalDetail.createRoute(goal.id)) }
                    )

                    Spacer(Modifier.height(24.dp))
                }

                // ── Section title ─────────────────────────────────────────────────
                Text(
                    text = "Quick Actions",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Four quick action buttons in a beautiful horizontal row ───────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardDark)
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.AttachMoney,
                        label = "Add Income",
                        iconBg = Brush.linearGradient(listOf(AccentGreen, Color(0xFF00C853))),
                        onClick = { navController.navigate(Screen.AddIncome.route) }
                    )
                    QuickActionItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Add,
                        label = "Add Expense",
                        iconBg = Brush.linearGradient(listOf(AccentOrange, Color(0xFFFF5722))),
                        onClick = { navController.navigate(Screen.AddExpense.route) }
                    )
                    QuickActionItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.TrackChanges,
                        label = "Goals",
                        iconBg = Brush.linearGradient(listOf(AccentPurple, Color(0xFFE040FB))),
                        onClick = { navController.navigate(Screen.GoalDetail.createRoute("active")) }
                    )
                    QuickActionItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = "Insights",
                        iconBg = Brush.linearGradient(listOf(AccentBlue, Color(0xFF00B4D8))),
                        onClick = { navController.navigate(Screen.Dashboard.route) }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Spending overview ─────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spending Overview",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View More",
                        color = AccentBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.TransactionHistory.createRoute("expense"))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.categoryBreakdown.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardDark)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses recorded yet. Tap 'Add Expense' to get started!",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    SpendingOverview(categoryBreakdown = uiState.categoryBreakdown)
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Income overview ─────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Income Overview",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View More",
                        color = AccentBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.TransactionHistory.createRoute("income"))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.incomeBreakdown.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardDark)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No incomes recorded yet. Tap 'Add Income' to get started!",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    IncomeOverview(incomeBreakdown = uiState.incomeBreakdown)
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    userName: String,
    onAvatarClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder — clickable to navigate to profile
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(AccentPurple, AccentBlue)))
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                userName.take(1).uppercase(),
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = "Welcome back,",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Text(
                text = userName,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.weight(1f))

        // Bell icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CardDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun BalanceCard(
    balance: Double,
    income: Double,
    expenses: Double,
    onCardClick: () -> Unit,
    onIncomeClick: () -> Unit,
    onExpensesClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardGradient)
            .clickable { onCardClick() }
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Total Balance",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "LKR %,.0f".format(balance),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BalanceStat(
                    label = "Income",
                    value = "LKR %,.0f".format(income),
                    color = AccentGreen,
                    onClick = onIncomeClick,
                    modifier = Modifier.weight(1f)
                )
                BalanceStat(
                    label = "Expenses",
                    value = "LKR %,.0f".format(expenses),
                    color = AccentOrange,
                    onClick = onExpensesClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BalanceStat(
    label: String,
    value: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuickActionItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    iconBg: Brush,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .scale(scale)
    ) {
        // Icon container with gradient and rounded shape
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
