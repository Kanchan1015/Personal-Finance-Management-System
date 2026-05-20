package com.example.pbd.ui.screens.income

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbd.ui.theme.PBDTheme

private val DarkBackground = Color(0xFF1C1C2E)
private val DarkCard = Color(0xFF2A2A3E)
private val DarkBorder = Color(0xFF3A3A4E)
private val LabelGray = Color(0xFF9E9E9E)
private val WhiteText = Color(0xFFFFFFFF)
private val GradientStart = Color(0xFF7B61FF)
private val GradientEnd = Color(0xFF00B4D8)

private val incomeCurrencies = listOf("LKR", "USD", "USDT")
private val incomeTypes = listOf("Salary", "Freelance", "Crypto")

@Composable
fun AddIncomeScreen(navController: NavHostController) {
    AddIncomeScreenContent(
        onClose = { navController.popBackStack() }
    )
}

@Composable
private fun AddIncomeScreenContent(
    onClose: () -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(incomeCurrencies.first()) }
    var selectedIncomeType by remember { mutableStateOf(incomeTypes.first()) }

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
            HeaderSection(onClose = onClose)

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            val isEnabled = amount.isNotEmpty() && amount != "."

            PrimaryActionButton(
                text = "Save Income",
                enabled = isEnabled
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection(onClose: () -> Unit) {
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
                text = "Income",
                color = WhiteText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DarkCard)
                .clickable { onClose() },
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
    enabled: Boolean
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
            .clickable(enabled = enabled) {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) WhiteText else LabelGray,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
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
