package com.example.pbd.ui.screens.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier,
    onBoost: ((Double) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val monthsRemaining = if (goal.deadline > 0) {
        val diff = goal.deadline - System.currentTimeMillis()
        if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff) / 30 else 0L
    } else 0L

    val progressPercent = (progress * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.title,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Savings Target",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$progressPercent%",
                        color = AccentGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$monthsRemaining months left",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AccentPurple,
                trackColor = DarkSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LKR ${"%,.0f".format(goal.currentSaved)}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "LKR ${"%,.0f".format(goal.targetAmount)}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Quick micro-savings boosters if callback is provided
            onBoost?.let { boostAction ->
                var showCustomBoostDialog by remember { mutableStateOf(false) }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = DarkSurface, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Goal Booster",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val boostAmounts = listOf(500.0, 1000.0, 5000.0)
                    boostAmounts.forEach { amount ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.94f else 1f,
                            animationSpec = tween(80),
                            label = "scale"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .scale(scale)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            AccentPurple.copy(alpha = 0.15f),
                                            AccentBlue.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = AccentPurple.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { boostAction(amount) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${"%,.0f".format(amount)}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // A fourth chip for Custom amount
                    val customInteractionSource = remember { MutableInteractionSource() }
                    val customIsPressed by customInteractionSource.collectIsPressedAsState()
                    val customScale by animateFloatAsState(
                        targetValue = if (customIsPressed) 0.94f else 1f,
                        animationSpec = tween(80),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .scale(customScale)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        AccentOrange.copy(alpha = 0.15f),
                                        AccentYellow.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = AccentOrange.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(
                                interactionSource = customInteractionSource,
                                indication = null,
                                onClick = { showCustomBoostDialog = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Custom",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                if (showCustomBoostDialog) {
                    var customAmount by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showCustomBoostDialog = false },
                        containerColor = DarkCard,
                        title = {
                            Text(
                                text = "Add Manual Savings",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Enter a custom LKR amount to save directly toward this goal.",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                                OutlinedTextField(
                                    value = customAmount,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                                            customAmount = newValue
                                        }
                                    },
                                    label = { Text("Savings Amount (LKR)", color = TextSecondary) },
                                    placeholder = { Text("e.g. 2500", color = TextHint) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentPurple,
                                        unfocusedBorderColor = TextSecondary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val amountDouble = customAmount.toDoubleOrNull() ?: 0.0
                                    if (amountDouble > 0.0) {
                                        boostAction(amountDouble)
                                    }
                                    showCustomBoostDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                enabled = customAmount.isNotEmpty()
                            ) {
                                Text("Add Savings")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCustomBoostDialog = false }) {
                                Text("Cancel", color = TextSecondary)
                            }
                        }
                    )
                }
            }
        }
    }
}