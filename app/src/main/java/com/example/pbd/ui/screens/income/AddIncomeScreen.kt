package com.example.pbd.ui.screens.income

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavHostController
import com.example.pbd.data.model.TransactionCategory
import com.example.pbd.ui.theme.PBDTheme

private val DarkBackground = Color(0xFF0D0F1A)
private val DarkCard       = Color(0xFF161929)
private val DarkBorder     = Color(0xFF212437)
private val LabelGray = Color(0xFF9E9E9E)
private val WhiteText = Color(0xFFFFFFFF)
private val GradientStart = Color(0xFF7B61FF)
private val GradientEnd = Color(0xFF00B4D8)

private val incomeCurrencies = listOf("LKR", "USD", "USDT")
private val incomeTypes = listOf("Salary", "Freelance", "Crypto")

@Composable
fun AddIncomeScreen(
    navController: NavHostController,
    viewModel: IncomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showInterceptSheet by remember { mutableStateOf(false) }
    var interceptedAmountLKR by remember { mutableStateOf(0.0) }
    var inputAmount by remember { mutableStateOf("") }
    var inputCurrency by remember { mutableStateOf("LKR") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val activeGoal = uiState.activeGoal
            val amountLKR = if (inputCurrency == "LKR") inputAmount.toDoubleOrNull() ?: 0.0 else uiState.convertedAmountLKR
            if (activeGoal != null && amountLKR > 0.0) {
                interceptedAmountLKR = amountLKR
                showInterceptSheet = true
            } else {
                android.widget.Toast.makeText(
                    context,
                    "Income added successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                navController.popBackStack()
                viewModel.resetState()
            }
        }
    }

    LaunchedEffect(uiState.isRoutingSuccess) {
        if (uiState.isRoutingSuccess) {
            val routed = interceptedAmountLKR * (uiState.savingsPercentage / 100.0)
            android.widget.Toast.makeText(
                context,
                "LKR ${"%,.0f".format(routed)} routed to ${uiState.activeGoal?.title}!",
                android.widget.Toast.LENGTH_LONG
            ).show()
            showInterceptSheet = false
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AddIncomeScreenContent(
            uiState = uiState,
            onClose = { navController.popBackStack() },
            onFetchExchangeRate = { amount, currency ->
                inputAmount = amount.toString()
                inputCurrency = currency
                viewModel.fetchExchangeRate(
                    amount = amount,
                    currency = currency
                )
            },
            onSaveIncome = { amount, currency, incomeType ->
                inputAmount = amount.toString()
                inputCurrency = currency
                viewModel.saveIncome(
                    amount = amount,
                    currency = currency,
                    category = incomeType.toTransactionCategory(),
                    subCategory = incomeType
                )
            }
        )

        if (showInterceptSheet) {
            val activeGoal = uiState.activeGoal
            if (activeGoal != null) {
                val routedAmount = interceptedAmountLKR * (uiState.savingsPercentage / 100.0)
                AlertDialog(
                    onDismissRequest = {
                        showInterceptSheet = false
                        android.widget.Toast.makeText(context, "Income added successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                        viewModel.resetState()
                    },
                    containerColor = DarkCard,
                    title = {
                        Text(
                            text = "Smart Savings Route",
                            color = WhiteText,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "LKR ${"%,.0f".format(interceptedAmountLKR)} income logged. Would you like to route ${uiState.savingsPercentage}% (LKR ${"%,.0f".format(routedAmount)}) directly to your ${activeGoal.title} fund?",
                                color = LabelGray,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.routeSavingsToGoal(interceptedAmountLKR, activeGoal, uiState.savingsPercentage)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                        ) {
                            Text("Route Savings")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showInterceptSheet = false
                                android.widget.Toast.makeText(context, "Income added successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                                viewModel.resetState()
                            }
                        ) {
                            Text("Keep Full Income", color = LabelGray)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AddIncomeScreenContent(
    uiState: IncomeUiState = IncomeUiState(),
    onClose: () -> Unit = {},
    onFetchExchangeRate: (amount: Double, currency: String) -> Unit = { _, _ -> },
    onSaveIncome: (amount: Double, currency: String, incomeType: String) -> Unit = { _, _, _ -> }
) {
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(incomeCurrencies.first()) }
    var selectedIncomeType by remember { mutableStateOf(incomeTypes.first()) }
    val isLoading = uiState.isLoading
    val amountValue = amount.toDoubleOrNull()

    LaunchedEffect(amount, selectedCurrency) {
        if (amountValue != null && amountValue > 0.0) {
            onFetchExchangeRate(amountValue, selectedCurrency)
        }
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
                HeaderSection(onClose = onClose)
            }

            // ── Scrollable Content Area ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {

            Text(text = "Amount", color = LabelGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            AmountField(
                amount = amount,
                currency = selectedCurrency,
                onAmountChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amount = newValue
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Currency", color = LabelGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            SelectionField(
                value = selectedCurrency,
                options = incomeCurrencies,
                onValueSelected = { selectedCurrency = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Income Type", color = LabelGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            SelectionField(
                value = selectedIncomeType,
                options = incomeTypes,
                onValueSelected = { selectedIncomeType = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ConversionPreviewCard(
                amount = amountValue,
                currency = selectedCurrency,
                uiState = uiState
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            val hasValidAmount = amountValue != null && amountValue > 0.0
            val hasResolvedConversion = selectedCurrency == "LKR" || uiState.convertedAmountLKR > 0.0
            val isEnabled = !isLoading &&
                !uiState.isExchangeRateLoading &&
                hasValidAmount &&
                hasResolvedConversion

            PrimaryActionButton(
                text = "Save Income",
                enabled = isEnabled,
                isLoading = isLoading,
                onClick = {
                    if (amountValue != null && amountValue > 0.0) {
                        onSaveIncome(amountValue, selectedCurrency, selectedIncomeType)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ConversionPreviewCard(
    amount: Double?,
    currency: String,
    uiState: IncomeUiState
) {
    if (amount == null || amount <= 0.0) {
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        when {
            uiState.isExchangeRateLoading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = WhiteText,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Fetching live exchange rate...",
                        color = LabelGray,
                        fontSize = 14.sp
                    )
                }
            }

            uiState.exchangeRateErrorMessage != null -> {
                Text(
                    text = uiState.exchangeRateErrorMessage,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp
                )
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${formatAmount(amount)} $currency \u2248 ${formatAmount(uiState.convertedAmountLKR)} LKR",
                        color = WhiteText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (currency == "LKR") {
                            "Direct local currency entry"
                        } else {
                            "1 $currency = ${formatAmount(uiState.exchangeRate)} LKR"
                        },
                        color = LabelGray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DarkCard)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = WhiteText,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "Add New",
                color = LabelGray,
                fontSize = 13.sp
            )
            Text(
                text = "Income",
                color = WhiteText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AmountField(
    amount: String,
    currency: String,
    onAmountChange: (String) -> Unit
) {
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
                text = currency,
                color = WhiteText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
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
}

@Composable
private fun SelectionField(
    value: String,
    options: List<String>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                color = WhiteText,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand",
                tint = LabelGray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkCard)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = WhiteText
                        )
                    },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                } else {
                    Brush.horizontalGradient(listOf(DarkCard, DarkCard))
                }
            )
            .clickable(enabled = enabled) { onClick() },
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
                text = text,
                color = if (enabled) WhiteText else LabelGray,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun String.toTransactionCategory(): TransactionCategory {
    return when (this) {
        "Salary" -> TransactionCategory.SALARY
        "Freelance" -> TransactionCategory.FREELANCE
        "Crypto" -> TransactionCategory.CRYPTO
        else -> TransactionCategory.SALARY
    }
}

private fun formatAmount(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format("%.2f", value)
    }
}

@Preview(showBackground = true)
@Composable
private fun AddIncomeScreenPreview() {
    PBDTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            AddIncomeScreenContent()
        }
    }
}
