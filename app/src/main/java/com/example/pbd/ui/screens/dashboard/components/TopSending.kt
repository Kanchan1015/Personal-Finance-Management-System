package com.example.pbd.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbd.ui.theme.*

@Composable
fun TopSpending(
    categoryBreakdown: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val total = categoryBreakdown.values.sum()
    val topCategories = categoryBreakdown.entries.take(5)

    val categoryColors = listOf(
        CategoryShopping, CategoryHousing,
        CategoryFood, CategoryTransport, CategoryOthers
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        topCategories.forEachIndexed { index, (category, amount) ->
            val color = categoryColors[index % categoryColors.size]
            val percentage = if (total > 0) (amount / total * 100).toInt() else 0

            TopSpendingItem(
                category = category,
                amount = amount,
                percentage = percentage,
                color = color
            )
        }
    }
}

@Composable
private fun TopSpendingItem(
    category: String,
    amount: Double,
    percentage: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "LKR ${"%,.0f".format(amount)}",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$percentage%",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = color,
            trackColor = DarkSurface
        )
    }
}