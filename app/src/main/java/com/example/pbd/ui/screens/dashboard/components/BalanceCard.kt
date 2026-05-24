package com.example.pbd.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbd.ui.theme.*

@Composable
fun BalanceCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpenses: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Total Balance",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LKR ${"%,.0f".format(totalBalance)}",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IncomeExpenseItem(
                    label = "Income",
                    amount = totalIncome,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = IncomeGreen
                )
                IncomeExpenseItem(
                    label = "Expenses",
                    amount = totalExpenses,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = ExpenseRed
                )
            }
        }
    }
}

@Composable
private fun IncomeExpenseItem(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
            Text(
                text = "LKR ${"%,.0f".format(amount)}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}