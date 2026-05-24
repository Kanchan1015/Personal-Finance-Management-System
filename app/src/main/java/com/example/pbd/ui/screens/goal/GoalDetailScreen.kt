package com.example.pbd.ui.screens.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbd.data.model.Goal
import com.example.pbd.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    navController: NavHostController,
    goalId: String,
    viewModel: GoalDetailViewModel = koinViewModel()
) {
    LaunchedEffect(goalId) {
        viewModel.loadGoal(goalId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToDelete by remember { mutableStateOf<Goal?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Goal Tracker",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddGoalDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Goal",
                            tint = AccentPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            }

            uiState.error != null && uiState.goals.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No savings goal yet",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Set a goal to track your savings progress",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddGoalDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPurple
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Set Savings Goal", fontSize = 15.sp)
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    uiState.goals.forEach { goal ->
                        GoalCard(
                            goal = goal,
                            onDelete = { goalToDelete = goal }
                        )
                    }

                    // Add another goal button at the bottom
                    OutlinedButton(
                        onClick = { showAddGoalDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AccentPurple
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, AccentPurple
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Add Another Goal", fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Add goal dialog
        if (showAddGoalDialog) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onConfirm = { title, target, months ->
                    viewModel.addGoal(title, target, months)
                    showAddGoalDialog = false
                }
            )
        }

        // Delete confirmation dialog
        goalToDelete?.let { goal ->
            AlertDialog(
                onDismissRequest = { goalToDelete = null },
                containerColor = DarkCard,
                title = {
                    Text(
                        text = "Delete Goal",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete \"${goal.title}\"? This cannot be undone.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteGoal(goal.id)
                            goalToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExpenseRed
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { goalToDelete = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onDelete: () -> Unit
) {
    val progressPercent = if (goal.targetAmount > 0) {
        ((goal.currentSaved / goal.targetAmount) * 100).toInt().coerceIn(0, 100)
    } else 0

    val monthsRemaining = if (goal.deadline > 0) {
        val diff = goal.deadline - System.currentTimeMillis()
        if (diff > 0) (diff / (30L * 24 * 60 * 60 * 1000)) else 0L
    } else 0L

    val amountRemaining = goal.targetAmount - goal.currentSaved
    val monthlyNeeded = if (monthsRemaining > 0) amountRemaining / monthsRemaining else amountRemaining
    val isOnTrack = progressPercent > 0 && monthsRemaining > 0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Hero card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = goal.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Savings Goal",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Goal",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$progressPercent%",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "of target reached",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Saved",
                value = "LKR ${"%,.0f".format(goal.currentSaved)}",
                color = IncomeGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Target",
                value = "LKR ${"%,.0f".format(goal.targetAmount)}",
                color = AccentPurple,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Months Left",
                value = "$monthsRemaining",
                color = AccentBlue,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Monthly Need",
                value = "LKR ${"%,.0f".format(monthlyNeeded)}",
                color = AccentOrange,
                modifier = Modifier.weight(1f)
            )
        }

        // Status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isOnTrack) Icons.Default.CheckCircle
                    else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isOnTrack) IncomeGreen else AccentOrange,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = if (isOnTrack) "On Track" else "Needs Attention",
                        color = if (isOnTrack) IncomeGreen else AccentOrange,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isOnTrack)
                            "You are making good progress toward your goal."
                        else
                            "Save LKR ${"%,.0f".format(monthlyNeeded)} per month to reach your goal.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Remaining amount
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Amount Remaining",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "LKR ${"%,.0f".format(amountRemaining)}",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(
            color = DarkCard,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("12") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Text(
                text = "New Savings Goal",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Name", color = TextSecondary) },
                    placeholder = { Text("e.g. MacBook Pro M4", color = TextHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text("Target Amount (LKR)", color = TextSecondary) },
                    placeholder = { Text("e.g. 490000", color = TextHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = months,
                    onValueChange = { months = it },
                    label = { Text("Target Months", color = TextSecondary) },
                    placeholder = { Text("e.g. 12", color = TextHint) },
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
                    val target = targetAmount.toDoubleOrNull() ?: return@Button
                    val monthsInt = months.toIntOrNull() ?: 12
                    if (title.isNotBlank()) {
                        onConfirm(title, target, monthsInt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}