package com.example.pbd.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbd.data.model.Transaction
import com.example.pbd.data.model.TransactionType
import com.example.pbd.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecentTransactions(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        transactions.forEach { transaction ->
            TransactionItem(transaction = transaction)
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val amountPrefix = if (isIncome) "+" else "-"

    val dateFormat = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(transaction.timestamp))

    val icon = when (transaction.subCategory.lowercase()) {
        "food" -> Icons.Default.ShoppingCart
        "housing", "rent" -> Icons.Default.Home
        else -> Icons.Default.AttachMoney
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = DarkCard,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.subCategory.ifEmpty { transaction.category.name },
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = dateString,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Text(
            text = "$amountPrefix LKR ${"%,.0f".format(transaction.baseAmountLKR)}",
            color = amountColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}