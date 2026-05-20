package com.example.pbd.ui.screens.expense

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.pbd.data.model.TransactionCategory
import com.example.pbd.ui.expense.ExpenseUiState
import com.example.pbd.ui.expense.ExpenseViewModel
import com.example.pbd.ui.expense.ExpenseViewModelFactory

// ─────────────────────────────────────────────────────────────────────────────
// Expense Sub-Categories
// Each one has a label, an icon, and maps to either COMMITTED or DISCRETIONARY.
// This is the core of the "frictionless" categorisation requirement.
// ─────────────────────────────────────────────────────────────────────────────
data class ExpenseSubCategory(
    val label: String,
    val icon: ImageVector,
    val mainCategory: TransactionCategory  // auto-assigned broad group
)

val expenseSubCategories = listOf(
    ExpenseSubCategory("Shopping",      Icons.Default.ShoppingCart,    TransactionCategory.DISCRETIONARY),
    ExpenseSubCategory("Transport",     Icons.Default.DirectionsCar,   TransactionCategory.DISCRETIONARY),
    ExpenseSubCategory("Food",          Icons.Default.Restaurant,      TransactionCategory.DISCRETIONARY),
    ExpenseSubCategory("Entertainment", Icons.Default.SportsEsports,   TransactionCategory.DISCRETIONARY),
    ExpenseSubCategory("Home",          Icons.Default.Home,            TransactionCategory.COMMITTED),
    ExpenseSubCategory("Bills",         Icons.Default.Receipt,         TransactionCategory.COMMITTED),
    ExpenseSubCategory("Coffee",        Icons.Default.LocalCafe,       TransactionCategory.DISCRETIONARY),
    ExpenseSubCategory("Gifts",         Icons.Default.CardGiftcard,    TransactionCategory.DISCRETIONARY),
    ExpenseSubCategory("Health",        Icons.Default.Favorite,        TransactionCategory.COMMITTED),
    ExpenseSubCategory("Other",         Icons.Default.MoreHoriz,       TransactionCategory.DISCRETIONARY),
)

// ─────────────────────────────────────────────────────────────────────────────
// Colour palette (dark theme matching the mockup)
// ─────────────────────────────────────────────────────────────────────────────
private val DarkBackground = Color(0xFF1C1C2E)
private val DarkCard       = Color(0xFF2A2A3E)
private val DarkBorder     = Color(0xFF3A3A4E)
private val LabelGray      = Color(0xFF9E9E9E)
private val WhiteText      = Color(0xFFFFFFFF)
private val GradientStart  = Color(0xFF7B61FF)   // purple
private val GradientEnd    = Color(0xFF00B4D8)   // cyan

// ─────────────────────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AddExpenseScreen(
    navController: NavHostController,
    viewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // UI state variables
    var amount by remember { mutableStateOf("") }
    var selectedSubCategory by remember { mutableStateOf(expenseSubCategories[0]) }
    var note by remember { mutableStateOf("") }

    val context = LocalContext.current

    // When save succeeds, show a success Toast and navigate back automatically
    LaunchedEffect(uiState) {
        if (uiState is ExpenseUiState.Success) {
            android.widget.Toast.makeText(context, "Expense added successfully!", android.widget.Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Add New",
                        color = LabelGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Expense",
                        color = WhiteText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Close / back button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = WhiteText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Amount Field ──────────────────────────────────────────────
            Text(text = "Amount", color = LabelGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LKR",
                        color = WhiteText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // BasicTextField gives full control over styling inside a dark card
                    BasicTextField(
                        value = amount,
                        onValueChange = { newValue ->
                            // Only allow valid decimal numbers
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                amount = newValue
                            }
                        },
                        textStyle = TextStyle(
                            color = LabelGray,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (amount.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    color = LabelGray,
                                    fontSize = 24.sp
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Category Grid ─────────────────────────────────────────────
            Text(text = "Category", color = LabelGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            CategoryGrid(
                categories = expenseSubCategories,
                selected = selectedSubCategory,
                onSelect = { selectedSubCategory = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Note / Description ────────────────────────────────────────
            Text(text = "Description", color = LabelGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    textStyle = TextStyle(color = WhiteText, fontSize = 15.sp),
                    decorationBox = { innerTextField ->
                        if (note.isEmpty()) {
                            Text(text = "Add a note...", color = LabelGray, fontSize = 15.sp)
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ── Error Message ─────────────────────────────────────────────
            if (uiState is ExpenseUiState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (uiState as ExpenseUiState.Error).message,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Gradient Save Button ──────────────────────────────────────
            val isLoading = uiState is ExpenseUiState.Loading
            val isEnabled = !isLoading && amount.isNotEmpty() && amount != "."

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = if (isEnabled) {
                            Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                        } else {
                            Brush.horizontalGradient(listOf(DarkCard, DarkCard))
                        }
                    )
                    .clickable(enabled = isEnabled) {
                        val amountDouble = amount.toDoubleOrNull() ?: 0.0
                        if (amountDouble > 0) {
                            viewModel.saveExpense(
                                amount = amountDouble,
                                category = selectedSubCategory.mainCategory,
                                subCategory = selectedSubCategory.label,
                                note = note
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = WhiteText,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Add Expense",
                        color = if (isEnabled) WhiteText else LabelGray,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category Grid — shows 5 icons per row, highlights the selected one
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CategoryGrid(
    categories: List<ExpenseSubCategory>,
    selected: ExpenseSubCategory,
    onSelect: (ExpenseSubCategory) -> Unit
) {
    // Split into rows of 5 (matches the mockup layout)
    val rows = categories.chunked(5)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { subCat ->
                    CategoryItem(
                        subCategory = subCat,
                        isSelected = subCat.label == selected.label,
                        onClick = { onSelect(subCat) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single Category Item — icon circle + label underneath
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CategoryItem(
    subCategory: ExpenseSubCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Purple highlight when selected, dark card otherwise
    val bgColor   = if (isSelected) GradientStart else DarkCard
    val iconTint  = if (isSelected) WhiteText else LabelGray
    val textColor = if (isSelected) WhiteText else LabelGray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = subCategory.icon,
                contentDescription = subCategory.label,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subCategory.label,
            color = textColor,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}
