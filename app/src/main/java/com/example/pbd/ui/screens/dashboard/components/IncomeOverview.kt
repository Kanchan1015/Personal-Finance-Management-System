package com.example.pbd.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbd.ui.theme.*

@Composable
fun IncomeOverview(
    incomeBreakdown: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val totalIncome = incomeBreakdown.values.sum()
    val incomes = incomeBreakdown.toList()

    val categoryColors = mapOf(
        "Salary" to IncomeGreen,
        "Freelance" to AccentGreen,
        "Crypto" to AccentBlue
    )

    val defaultColors = listOf(
        IncomeGreen, AccentGreen, AccentBlue, AccentPurple, AccentOrange
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        incomes.forEach { (category, amount) ->
            val progress = if (totalIncome > 0) (amount / totalIncome).toFloat() else 0f
            val color = categoryColors[category]
                ?: defaultColors[incomes.indexOf(Pair(category, amount)) % defaultColors.size]

            IncomeCard(
                label = category.lowercase().replaceFirstChar { it.uppercase() },
                amount = "LKR %,.0f".format(amount),
                color = color,
                progress = progress,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 120.dp)
            )
        }
    }
}

@Composable
private fun IncomeCard(
    label: String,
    amount: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
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
        Spacer(modifier = Modifier.weight(1f))
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
