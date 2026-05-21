package com.example.pbd.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun SpendingOverview(
    categoryBreakdown: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val categoryColors = mapOf(
        "Shopping" to CategoryShopping,
        "Housing" to CategoryHousing,
        "Food" to CategoryFood,
        "Transport" to CategoryTransport,
        "COMMITTED" to CategoryHousing,
        "DISCRETIONARY" to CategoryOthers
    )

    val defaultColors = listOf(
        CategoryShopping, CategoryHousing,
        CategoryFood, CategoryTransport, CategoryOthers
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(categoryBreakdown.entries.toList()) { (category, amount) ->
            val color = categoryColors[category]
                ?: defaultColors[categoryBreakdown.keys.indexOf(category) % defaultColors.size]

            CategoryChip(
                label = category,
                amount = amount,
                color = color
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    amount: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .padding(12.dp)
            .width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${"%,.0f".format(amount / 1000)}K",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}