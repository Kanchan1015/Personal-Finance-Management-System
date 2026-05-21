package com.example.pbd.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbd.data.model.Goal
import com.example.pbd.ui.theme.*
import java.util.concurrent.TimeUnit

@Composable
fun GoalCard(
    goal: Goal,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val monthsRemaining = if (goal.deadline > 0) {
        val diff = goal.deadline - System.currentTimeMillis()
        TimeUnit.MILLISECONDS.toDays(diff) / 30
    } else 0L

    val progressPercent = (progress * 100).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Savings Goal",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$progressPercent%",
                        color = AccentGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$monthsRemaining months",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = AccentOrange,
                trackColor = DarkSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LKR ${"%,.0f".format(goal.currentSaved)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "LKR ${"%,.0f".format(goal.targetAmount)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}