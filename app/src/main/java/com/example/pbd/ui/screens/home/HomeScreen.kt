package com.example.pbd.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.List
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
                    onAvatarClick = { navController.navigate(Screen.Dashboard.route) }
                )

                Spacer(Modifier.height(24.dp))

                // ── Balance card ─────────────────────────────────────────────────
                BalanceCard(
                    balance = uiState.netBalance,
                    income = uiState.totalIncome,
                    expenses = uiState.totalExpenses,
                    onClick = { navController.navigate(Screen.Dashboard.route) }
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

                    Spacer(Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Compile Acceleration: Tapping Boost gets you your high-speed MacBook compiler sooner!",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }

                // ── Section title ─────────────────────────────────────────────────
                Text(
                    text = "Quick Actions",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                // ── Two action buttons ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.List,
                        label = "View Expenses",
                        description = "See all recorded expenses",
                        iconBg = Brush.linearGradient(listOf(AccentBlue, AccentPurple)),
                        onClick = { navController.navigate(Screen.TransactionHistory.route) }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Add,
                        label = "Add Expense",
                        description = "Log a new expense entry",
                        iconBg = Brush.linearGradient(listOf(AccentOrange, Color(0xFFFF5722))),
                        onClick = { navController.navigate(Screen.AddExpense.route) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.AttachMoney,
                        label = "Add Income",
                        description = "Record a new income entry",
                        iconBg = Brush.linearGradient(listOf(AccentGreen, AccentBlue)),
                        onClick = { navController.navigate(Screen.AddIncome.route) }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.TrackChanges,
                        label = "Goal Tracker",
                        description = "View your savings goal",
                        iconBg = Brush.linearGradient(listOf(AccentPurple, Color(0xFFE040FB))),
                        onClick = { navController.navigate(Screen.GoalDetail.createRoute("active")) }
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── Spending overview ─────────────────────────────────────────────
                Text(
                    text = "Spending Overview",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                val categories = uiState.categoryBreakdown.toList()
                if (categories.isEmpty()) {
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
                    val rows = categories.chunked(3)
                    rows.forEachIndexed { rowIndex, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { (category, amount) ->
                                val progress = if (uiState.totalExpenses > 0) (amount / uiState.totalExpenses).toFloat() else 0f
                                SpendingCard(
                                    label = category.lowercase().replaceFirstChar { it.uppercase() },
                                    amount = "LKR %,.0f".format(amount),
                                    color = getCategoryColor(category),
                                    progress = progress,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size < 3) {
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        if (rowIndex < rows.size - 1) {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
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
        // Avatar placeholder — clickable to navigate to dashboard
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
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardGradient)
            .clickable { onClick() }
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
            Row(modifier = Modifier.fillMaxWidth()) {
                BalanceStat(
                    label = "Income",
                    value = "LKR %,.0f".format(income),
                    change = "Base currency",
                    color = AccentGreen,
                    modifier = Modifier.weight(1f)
                )
                BalanceStat(
                    label = "Expenses",
                    value = "LKR %,.0f".format(expenses),
                    change = "Base currency",
                    color = AccentOrange,
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
    change: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = change, color = color, fontSize = 10.sp)
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    description: String,
    iconBg: Brush,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
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
            Spacer(Modifier.height(14.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun SpendingCard(
    label: String,
    amount: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(2.dp))
        Text(text = amount, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        // Mini bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "ESSENTIAL" -> AccentBlue
        "DISCRETIONARY" -> AccentGreen
        "SAVINGS" -> AccentPurple
        "DEBT" -> Color(0xFFE91E63)
        "INVESTMENT" -> AccentOrange
        else -> TextSecondary
    }
}
