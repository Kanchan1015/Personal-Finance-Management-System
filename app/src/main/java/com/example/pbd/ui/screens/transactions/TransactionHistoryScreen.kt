package com.example.pbd.ui.screens.transactions

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import com.example.pbd.data.model.TransactionType
import com.example.pbd.data.model.TransactionCategory
import com.example.pbd.ui.screens.expense.expenseSubCategories
import com.example.pbd.ui.screens.expense.ExpenseSubCategory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.pbd.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

// ── Colour Palette (Aesthetic Dark Theme matching the screenshots) ───────────────
private val BackgroundDark    = Color(0xFF0F101C)
private val CardDark          = Color(0xFF181B2B)
private val CardDarker        = Color(0xFF111325)
private val TabUnselectedCard = Color(0xFF212437)
private val TabSelectedCard   = Color(0xFF32364D)
private val TextWhite         = Color(0xFFFFFFFF)
private val TextSecondary     = Color(0xFF8C91A5)
private val GradientPurple    = Color(0xFF7B61FF)
private val GradientCyan      = Color(0xFF00B4D8)
private val IncomeGreen       = Color(0xFF4CAF50)
private val ExpenseRed        = Color(0xFFFF6B6B)
private val DarkBorder        = Color(0xFF252840)

private val CardGradient = Brush.linearGradient(
    colors = listOf(GradientPurple, GradientCyan)
)

enum class FilterPeriod {
    ALL, TODAY, WEEK, MONTH
}

enum class SortFactor {
    DATE, PRICE
}

enum class SortOrder {
    ASCENDING, DESCENDING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    navController: NavHostController,
    mode: String = "all",
    viewModel: TransactionHistoryViewModel = viewModel(
        factory = TransactionHistoryViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val transactions by viewModel.transactionsState.collectAsState()
    var selectedFilter by remember { mutableStateOf(FilterPeriod.ALL) }
    var selectedSortFactor by remember { mutableStateOf(SortFactor.DATE) }
    var selectedSortOrder by remember { mutableStateOf(SortOrder.DESCENDING) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Transaction detail/edit/delete state
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Filter transactions based on selected tab and history mode (expense, income, or all)
    val filteredTransactions = remember(transactions, selectedFilter, selectedSortFactor, selectedSortOrder) {
        val baseFiltered = when (mode) {
            "expense" -> transactions.filter { it.type == TransactionType.EXPENSE }
            "income" -> transactions.filter { it.type == TransactionType.INCOME }
            else -> transactions
        }

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val periodFiltered = when (selectedFilter) {
            FilterPeriod.ALL -> baseFiltered
            FilterPeriod.TODAY -> {
                baseFiltered.filter {
                    val txCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                    txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                            txCal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
                }
            }
            FilterPeriod.WEEK -> {
                val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
                baseFiltered.filter { it.timestamp >= sevenDaysAgo }
            }
            FilterPeriod.MONTH -> {
                baseFiltered.filter {
                    val txCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                    txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                            txCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                }
            }
        }

        // Apply Sorting
        when (selectedSortFactor) {
            SortFactor.DATE -> {
                if (selectedSortOrder == SortOrder.DESCENDING) {
                    periodFiltered.sortedByDescending { it.timestamp }
                } else {
                    periodFiltered.sortedBy { it.timestamp }
                }
            }
            SortFactor.PRICE -> {
                if (selectedSortOrder == SortOrder.DESCENDING) {
                    periodFiltered.sortedByDescending { it.baseAmountLKR }
                } else {
                    periodFiltered.sortedBy { it.baseAmountLKR }
                }
            }
        }
    }

    // Calculate total income and expenses for the filtered list
    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.baseAmountLKR }
    }
    val totalExpenses = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.baseAmountLKR }
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
            HeaderRow(
                mode = mode,
                onBackClick = { navController.popBackStack() },
                onFilterClick = { showFilterDialog = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Gradient Summary Card ────────────────────────────────────────
            TotalSummaryCard(
                mode = mode,
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
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
                            text = "No transactions recorded",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                if (selectedSortFactor == SortFactor.DATE) {
                    // Group transactions by date string when sorting by date
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
                                TransactionListItem(
                                    transaction = transaction,
                                    onClick = {
                                        selectedTransaction = transaction
                                        showDetailDialog = true
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Continuous sequential flat list when sorting by amount
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredTransactions, key = { it.id }) { transaction ->
                            TransactionListItem(
                                transaction = transaction,
                                onClick = {
                                    selectedTransaction = transaction
                                    showDetailDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Filter/Sort Dialog ───────────────────────────────────────────────
        if (showFilterDialog) {
            FilterSortDialog(
                currentFactor = selectedSortFactor,
                currentOrder = selectedSortOrder,
                onDismiss = { showFilterDialog = false },
                onApply = { factor, order ->
                    selectedSortFactor = factor
                    selectedSortOrder = order
                    showFilterDialog = false
                }
            )
        }

        // ── Transaction Detail Dialog ────────────────────────────────────────
        val tx = selectedTransaction
        if (showDetailDialog && tx != null) {
            TransactionDetailDialog(
                transaction = tx,
                onDismiss = {
                    showDetailDialog = false
                    selectedTransaction = null
                },
                onEditClick = {
                    showDetailDialog = false
                    showEditDialog = true
                },
                onDeleteClick = {
                    showDetailDialog = false
                    showDeleteDialog = true
                }
            )
        }

        // ── Delete Confirmation Dialog ───────────────────────────────────────
        if (showDeleteDialog && tx != null) {
            DeleteConfirmationDialog(
                onConfirm = {
                    viewModel.deleteTransaction(tx.id)
                    showDeleteDialog = false
                    selectedTransaction = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    selectedTransaction = null
                }
            )
        }

        // ── Edit Transaction Dialog ──────────────────────────────────────────
        if (showEditDialog && tx != null) {
            EditTransactionDialog(
                transaction = tx,
                onDismiss = {
                    showEditDialog = false
                    selectedTransaction = null
                },
                onSave = { updatedTransaction ->
                    viewModel.updateTransaction(updatedTransaction)
                    showEditDialog = false
                    selectedTransaction = null
                }
            )
        }
    }
}

// ── Sub-Composables ───────────────────────────────────────────────────────────

@Composable
private fun HeaderRow(
    mode: String,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit
) {
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = when (mode) {
                        "expense" -> "Total Spending"
                        "income" -> "Total Income"
                        else -> "Transactions"
                    },
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = when (mode) {
                        "expense" -> "Expenses"
                        "income" -> "Incomes"
                        else -> "All History"
                    },
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
                .background(CardDark)
                .clickable { onFilterClick() },
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
private fun TotalSummaryCard(mode: String, totalIncome: Double, totalExpenses: Double, count: Int) {
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
                    imageVector = when (mode) {
                        "expense" -> Icons.Default.ArrowDownward
                        "income" -> Icons.Default.ArrowUpward
                        else -> Icons.AutoMirrored.Filled.TrendingDown
                    },
                    contentDescription = "Transactions",
                    tint = TextWhite.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (mode) {
                        "expense" -> "Total Expenses Summary"
                        "income" -> "Total Incomes Summary"
                        else -> "Transaction Summary"
                    },
                    color = TextWhite.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Net balance or specific total
            Text(
                text = when (mode) {
                    "expense" -> "LKR %,.0f".format(totalExpenses)
                    "income" -> "LKR %,.0f".format(totalIncome)
                    else -> "LKR %,.0f".format(totalIncome - totalExpenses)
                },
                color = TextWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (mode == "all") {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        text = "+LKR %,.0f  Income".format(totalIncome),
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "-LKR %,.0f  Expenses".format(totalExpenses),
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = when (mode) {
                        "expense" -> "Total recorded cash out"
                        "income" -> "Total recorded cash in"
                        else -> ""
                    },
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
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
private fun TransactionListItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val resolvedSubCategory = transaction.subCategory.ifEmpty {
        transaction.category.name.lowercase().replaceFirstChar { it.uppercase() }
    }
    val (icon, color) = getCategoryMeta(resolvedSubCategory)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardDark)
            .clickable { onClick() }
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
                contentDescription = resolvedSubCategory,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Transaction Details
        Column(modifier = Modifier.weight(1f)) {
            val displayName = transaction.note.ifEmpty { resolvedSubCategory }
            Text(
                text = displayName,
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = resolvedSubCategory,
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

        // Amount Label — green +LKR for income, red -LKR for expenses
        val isIncome = transaction.type == TransactionType.INCOME
        Text(
            text = "%sLKR %,.0f".format(if (isIncome) "+" else "-", transaction.baseAmountLKR),
            color = if (isIncome) IncomeGreen else ExpenseRed,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Transaction Detail Dialog ─────────────────────────────────────────────────

@Composable
fun TransactionDetailDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val resolvedSubCategory = transaction.subCategory.ifEmpty {
        transaction.category.name.lowercase().replaceFirstChar { it.uppercase() }
    }
    val (icon, color) = getCategoryMeta(resolvedSubCategory)
    val isIncome = transaction.type == TransactionType.INCOME

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(CardDark)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Category Icon ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.18f))
                        .border(1.5.dp, color.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = resolvedSubCategory,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Category Title ───────────────────────────────────────────
                Text(
                    text = resolvedSubCategory,
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // ── Type Badge (Income / Expense) ────────────────────────────
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isIncome) "Income" else "Expense",
                        color = if (isIncome) IncomeGreen else ExpenseRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Divider ──────────────────────────────────────────────────
                HorizontalDivider(color = DarkBorder, thickness = 0.8.dp)

                Spacer(modifier = Modifier.height(20.dp))

                // ── Amount ───────────────────────────────────────────────────
                // Split into currency prefix (small) + number (large) so the
                // full string never wraps on narrow screens.
                val amountColor = if (isIncome) IncomeGreen else ExpenseRed
                val signPrefix  = if (isIncome) "+" else "-"

                // Currency + sign row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = signPrefix,
                        color = amountColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = transaction.currency,
                        color = amountColor.copy(alpha = 0.75f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Number row — uses a font size that always fits on one line
                Text(
                    text = "%,.2f".format(transaction.baseAmountLKR),
                    color = amountColor,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                if (transaction.currency != "LKR") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Original: %s %,.2f  (rate: %.4f)".format(transaction.currency, transaction.amount, transaction.exchangeRate),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Details Grid ─────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDarker)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date",
                        value = formatFullDate(transaction.timestamp)
                    )
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Time",
                        value = formatTime(transaction.timestamp)
                    )
                    if (transaction.note.isNotBlank()) {
                        HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                        DetailRow(
                            icon = Icons.Default.Notes,
                            label = "Note",
                            value = transaction.note
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Action Buttons ───────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Delete Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ExpenseRed.copy(alpha = 0.12f))
                            .border(1.dp, ExpenseRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { onDeleteClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = ExpenseRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delete",
                                color = ExpenseRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Edit Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(GradientPurple, GradientCyan))
                            )
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Edit",
                                tint = TextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Close link
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Close",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(GradientPurple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GradientPurple,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Delete Confirmation Dialog ────────────────────────────────────────────────

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ExpenseRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete",
                    tint = ExpenseRed,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Delete Transaction",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this transaction? This action cannot be undone.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Text(
                    text = "Delete Transaction",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Text("Keep Transaction", color = TextSecondary, fontSize = 14.sp)
            }
        }
    )
}

// ── Edit Transaction Dialog ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME

    // Local editable state — pre-filled from the existing transaction
    var amount by remember {
        mutableStateOf(
            if (transaction.amount % 1.0 == 0.0) transaction.amount.toLong().toString()
            else "%.2f".format(transaction.amount)
        )
    }
    var note by remember { mutableStateOf(transaction.note) }

    // Subcategory state
    val incomeSubCategories = listOf("Salary", "Freelance", "Crypto")
    var selectedExpenseSub by remember {
        mutableStateOf(
            expenseSubCategories.find { it.label.equals(transaction.subCategory, ignoreCase = true) }
                ?: expenseSubCategories[0]
        )
    }
    var selectedIncomeSub by remember { mutableStateOf(transaction.subCategory.ifEmpty { "Salary" }) }

    // Date state — initialised from the transaction's timestamp
    var selectedTimestamp by remember { mutableLongStateOf(transaction.timestamp) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Determine if the current timestamp matches today or yesterday for the quick-chip display
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
    }.timeInMillis
    val yesterdayStart = todayStart - 86_400_000L

    var dateLabel by remember {
        mutableStateOf(
            when {
                selectedTimestamp >= todayStart -> "Today"
                selectedTimestamp >= yesterdayStart -> "Yesterday"
                else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedTimestamp))
            }
        )
    }

    // Validation
    val amountValid = amount.toDoubleOrNull()?.let { it > 0.0 } == true

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(CardDark)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // ── Header ───────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edit",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Transaction",
                            color = TextWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isIncome) "Income" else "Expense",
                            color = if (isIncome) IncomeGreen else ExpenseRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = DarkBorder, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // ── Amount Field ─────────────────────────────────────────────
                Text(text = "Amount", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDarker)
                        .border(
                            1.dp,
                            if (amountValid) DarkBorder else ExpenseRed.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.currency,
                            color = GradientPurple,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = amount,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    amount = newValue
                                }
                            },
                            textStyle = TextStyle(
                                color = TextWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (amount.isEmpty()) {
                                    Text(text = "0.00", color = TextSecondary, fontSize = 22.sp)
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Category / Subcategory ───────────────────────────────────
                Text(text = "Category", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))

                if (isIncome) {
                    // Income subcategory selection — 3 chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        incomeSubCategories.forEach { label ->
                            val isSelected = label == selectedIncomeSub
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) GradientPurple else CardDarker)
                                    .border(1.dp, if (isSelected) Color.Transparent else DarkBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedIncomeSub = label },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) TextWhite else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                } else {
                    // Expense subcategory grid — mirrors AddExpenseScreen layout
                    val rows = expenseSubCategories.chunked(5)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowItems.forEach { subCat ->
                                    val isSelected = subCat.label == selectedExpenseSub.label
                                    val activeColor = getExpenseCategoryColor(subCat.label)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(56.dp)
                                            .clickable { selectedExpenseSub = subCat }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(13.dp))
                                                .background(if (isSelected) activeColor else CardDarker)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color.Transparent else DarkBorder,
                                                    RoundedCornerShape(13.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = subCat.icon,
                                                contentDescription = subCat.label,
                                                tint = if (isSelected) TextWhite else TextSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = subCat.label,
                                            color = if (isSelected) activeColor else TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Date Selector ────────────────────────────────────────────
                Text(text = "Date", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf("Today", "Yesterday")
                    chips.forEach { chipLabel ->
                        val isSelected = dateLabel == chipLabel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GradientPurple else CardDarker)
                                .border(1.dp, if (isSelected) Color.Transparent else DarkBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    dateLabel = chipLabel
                                    val cal = Calendar.getInstance()
                                    if (chipLabel == "Yesterday") cal.add(Calendar.DAY_OF_YEAR, -1)
                                    selectedTimestamp = cal.timeInMillis
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chipLabel,
                                color = if (isSelected) TextWhite else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Custom date chip
                    val isCustomDate = dateLabel != "Today" && dateLabel != "Yesterday"
                    Box(
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCustomDate) GradientPurple else CardDarker)
                            .border(1.dp, if (isCustomDate) Color.Transparent else DarkBorder, RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pick date",
                                tint = if (isCustomDate) TextWhite else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCustomDate) dateLabel else "Other...",
                                color = if (isCustomDate) TextWhite else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Date Picker Dialog
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = selectedTimestamp
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    selectedTimestamp = millis
                                    dateLabel = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(millis))
                                }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Note / Description ───────────────────────────────────────
                Text(text = "Description", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDarker)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = note,
                        onValueChange = { note = it },
                        textStyle = TextStyle(color = TextWhite, fontSize = 14.sp),
                        decorationBox = { innerTextField ->
                            if (note.isEmpty()) {
                                Text(text = "Add a note...", color = TextSecondary, fontSize = 14.sp)
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Save Button ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (amountValid)
                                Brush.horizontalGradient(listOf(GradientPurple, GradientCyan))
                            else
                                Brush.horizontalGradient(listOf(DarkBorder, DarkBorder))
                        )
                        .clickable(enabled = amountValid) {
                            val newAmountDouble = amount.toDoubleOrNull() ?: return@clickable

                            // Recalculate LKR base amount using the original exchange rate
                            val newBaseAmountLKR = newAmountDouble * transaction.exchangeRate

                            // Resolve category from the chosen subcategory
                            val newCategory = if (isIncome) {
                                when (selectedIncomeSub) {
                                    "Freelance" -> TransactionCategory.FREELANCE
                                    "Crypto"    -> TransactionCategory.CRYPTO
                                    else        -> TransactionCategory.SALARY
                                }
                            } else {
                                selectedExpenseSub.mainCategory
                            }

                            val newSubCategory = if (isIncome) selectedIncomeSub else selectedExpenseSub.label

                            val updatedTransaction = transaction.copy(
                                amount        = newAmountDouble,
                                baseAmountLKR = newBaseAmountLKR,
                                category      = newCategory,
                                subCategory   = newSubCategory,
                                note          = note,
                                timestamp     = selectedTimestamp,
                                isSynced      = false   // mark dirty so SyncWorker re-pushes it
                            )
                            onSave(updatedTransaction)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save Changes",
                        color = if (amountValid) TextWhite else TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

// ── Filter/Sort Dialog ────────────────────────────────────────────────────────

@Composable
fun FilterSortDialog(
    currentFactor: SortFactor,
    currentOrder: SortOrder,
    onDismiss: () -> Unit,
    onApply: (SortFactor, SortOrder) -> Unit
) {
    var selectedFactor by remember { mutableStateOf(currentFactor) }
    var selectedOrder by remember { mutableStateOf(currentOrder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Sort & Filter",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Sort By Section
                Column {
                    Text(
                        text = "Sort By",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date Button
                        val isDate = selectedFactor == SortFactor.DATE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDate) GradientPurple else TabUnselectedCard)
                                .clickable { selectedFactor = SortFactor.DATE },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Date",
                                color = if (isDate) TextWhite else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isDate) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        // Price Button
                        val isPrice = selectedFactor == SortFactor.PRICE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPrice) GradientPurple else TabUnselectedCard)
                                .clickable { selectedFactor = SortFactor.PRICE },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Amount",
                                color = if (isPrice) TextWhite else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isPrice) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Order Section
                Column {
                    Text(
                        text = "Order",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Descending Button
                        val isDesc = selectedOrder == SortOrder.DESCENDING
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDesc) GradientCyan else TabUnselectedCard)
                                .clickable { selectedOrder = SortOrder.DESCENDING },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedFactor == SortFactor.DATE) "Newest First" else "Highest First",
                                color = if (isDesc) TextWhite else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isDesc) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        // Ascending Button
                        val isAsc = selectedOrder == SortOrder.ASCENDING
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAsc) GradientCyan else TabUnselectedCard)
                                .clickable { selectedOrder = SortOrder.ASCENDING },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedFactor == SortFactor.DATE) "Oldest First" else "Lowest First",
                                color = if (isAsc) TextWhite else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isAsc) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(selectedFactor, selectedOrder) },
                colors = ButtonDefaults.buttonColors(containerColor = GradientPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply", color = TextWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
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

private fun formatFullDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
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
        "salary"        -> Icons.Default.Work to Color(0xFF4CAF50) // Green
        "freelance"     -> Icons.Default.Computer to Color(0xFF00B4D8) // Blue/Cyan
        "crypto"        -> Icons.Default.AccountBalanceWallet to Color(0xFFFF9800) // Orange
        else            -> Icons.Default.MoreHoriz to Color(0xFF8C91A5) // Slate Gray
    }
}

private fun getExpenseCategoryColor(subCategory: String): Color {
    return when (subCategory.lowercase(Locale.getDefault())) {
        "shopping"      -> Color(0xFF7B61FF)
        "transport"     -> Color(0xFF00B4D8)
        "food"          -> Color(0xFFFF9800)
        "entertainment" -> Color(0xFF4CAF50)
        "home"          -> Color(0xFFFFEB3B)
        "bills"         -> Color(0xFFE91E63)
        "coffee"        -> Color(0xFF8D6E63)
        "gifts"         -> Color(0xFFAB47BC)
        "health"        -> Color(0xFF26A69A)
        else            -> Color(0xFF7B61FF)
    }
}