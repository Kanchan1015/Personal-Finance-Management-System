package com.example.pbd.ui.screens.transactions

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.pbd.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

// ── Colour Palette (Aesthetic Dark Theme matching the screenshots) ───────────────
private val BackgroundDark    = Color(0xFF0F101C)
private val CardDark          = Color(0xFF181B2B)
private val TabUnselectedCard = Color(0xFF212437)
private val TabSelectedCard   = Color(0xFF32364D)
private val TextWhite         = Color(0xFFFFFFFF)
private val TextSecondary     = Color(0xFF8C91A5)
private val GradientPurple    = Color(0xFF7B61FF)
private val GradientCyan      = Color(0xFF00B4D8)

private val CardGradient = Brush.linearGradient(
    colors = listOf(GradientPurple, GradientCyan)
)

enum class FilterPeriod {
    ALL, TODAY, WEEK, MONTH
}

@Composable
fun TransactionHistoryScreen(
    navController: NavHostController,
    viewModel: TransactionHistoryViewModel = viewModel(
        factory = TransactionHistoryViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val transactions by viewModel.transactionsState.collectAsState()
    var selectedFilter by remember { mutableStateOf(FilterPeriod.ALL) }

    // Filter transactions based on selected tab
    val filteredTransactions = remember(transactions, selectedFilter) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        when (selectedFilter) {
            FilterPeriod.ALL -> transactions
            FilterPeriod.TODAY -> {
                transactions.filter {
                    val txCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                    txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                            txCal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
                }
            }
            FilterPeriod.WEEK -> {
                val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
                transactions.filter { it.timestamp >= sevenDaysAgo }
            }
            FilterPeriod.MONTH -> {
                transactions.filter {
                    val txCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                    txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                            txCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                }
            }
        }
    }

    // Calculate total spending for filtered list
    val totalSpending = remember(filteredTransactions) {
        filteredTransactions.sumOf { it.baseAmountLKR }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Toolbar Header ───────────────────────────────────────────────
            HeaderRow(onBackClick = { navController.popBackStack() })

            Spacer(modifier = Modifier.height(20.dp))

            // ── Gradient Total Expenses Card ─────────────────────────────────
            TotalExpensesCard(
                amount = totalSpending,
                count = filteredTransactions.size
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Filter Pills/Tabs ────────────────────────────────────────────
            FilterTabs(
                selected = selectedFilter,
                onSelectedChange = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Transactions Lazy List ───────────────────────────────────────
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No Transactions",
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No expenses recorded",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                // Group transactions by date string
                val groupedTransactions = remember(filteredTransactions) {
                    filteredTransactions.groupBy { getGroupHeader(it.timestamp) }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    groupedTransactions.forEach { (dateHeader, txs) ->
                        item {
                            DateHeaderItem(title = dateHeader)
                        }
                        items(txs, key = { it.id }) { transaction ->
                            TransactionListItem(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-Composables ───────────────────────────────────────────────────────────

@Composable
private fun HeaderRow(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Total Spending",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "This Month",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Funnel Filter Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter Options",
                tint = TextWhite,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TotalExpensesCard(amount: Double, count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardGradient)
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = "Trending Down",
                    tint = TextWhite.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Total Expenses",
                    color = TextWhite.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "LKR %,.0f".format(amount),
                color = TextWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count transactions",
                color = TextWhite.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun FilterTabs(
    selected: FilterPeriod,
    onSelectedChange: (FilterPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPeriod.values().forEach { period ->
            val isSelected = selected == period
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) TabSelectedCard else TabUnselectedCard,
                label = "bgColor"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextWhite else TextSecondary,
                label = "textColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onSelectedChange(period) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (period) {
                        FilterPeriod.ALL -> "All"
                        FilterPeriod.TODAY -> "Today"
                        FilterPeriod.WEEK -> "Week"
                        FilterPeriod.MONTH -> "Month"
                    },
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DateHeaderItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = "Date",
            tint = TextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(TextSecondary.copy(alpha = 0.2f))
        )
    }
}

@Composable
private fun TransactionListItem(transaction: Transaction) {
    val (icon, color) = getCategoryMeta(transaction.subCategory)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Round Icon Circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = transaction.subCategory,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Transaction Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note.ifEmpty { transaction.subCategory },
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = transaction.subCategory,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = " • ",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = formatTime(transaction.timestamp),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Amount Label
        Text(
            text = "-LKR %,.0f".format(transaction.baseAmountLKR),
            color = TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Utility Helper Functions ───────────────────────────────────────────────────

private fun getGroupHeader(timestamp: Long): String {
    val now = Calendar.getInstance()
    val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        now.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == txCal.get(Calendar.DAY_OF_YEAR) -> "Today"
        
        now.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - txCal.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"
        
        else -> {
            val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun getCategoryMeta(subCategory: String): Pair<ImageVector, Color> {
    return when (subCategory.lowercase(Locale.getDefault())) {
        "shopping"      -> Icons.Default.ShoppingCart to Color(0xFF7B61FF) // Purple
        "transport"     -> Icons.Default.DirectionsCar to Color(0xFF00B4D8) // Blue/Cyan
        "food"          -> Icons.Default.Restaurant to Color(0xFFFF9800) // Orange
        "entertainment" -> Icons.Default.SportsEsports to Color(0xFF4CAF50) // Green
        "home"          -> Icons.Default.Home to Color(0xFFFFEB3B) // Yellow
        "bills"         -> Icons.Default.Receipt to Color(0xFFE91E63) // Pink/Red
        "coffee"        -> Icons.Default.LocalCafe to Color(0xFF8D6E63) // Brown
        "gifts"         -> Icons.Default.CardGiftcard to Color(0xFFAB47BC) // Lavender
        "health"        -> Icons.Default.Favorite to Color(0xFF26A69A) // Teal
        else            -> Icons.Default.MoreHoriz to Color(0xFF8C91A5) // Slate Gray
    }
}