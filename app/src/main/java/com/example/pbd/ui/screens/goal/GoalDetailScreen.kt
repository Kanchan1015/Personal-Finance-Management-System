package com.example.pbd.ui.screens.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Goal Details",
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

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Something went wrong",
                        color = ExpenseRed,
                        fontSize = 16.sp
                    )
                }
            }

            uiState.goal != null -> {
                val goal = uiState.goal!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Goal hero card
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
                            Text(
                                text = goal.title,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Savings Goal",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "${uiState.progressPercent}%",
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "of target reached",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { uiState.progressPercent / 100f },
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
                            value = "${uiState.monthsRemaining}",
                            color = AccentBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Monthly Need",
                            value = "LKR ${"%,.0f".format(uiState.monthlyTargetNeeded)}",
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
                                imageVector = if (uiState.isOnTrack)
                                    Icons.Default.CheckCircle
                                else
                                    Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (uiState.isOnTrack) IncomeGreen else AccentOrange,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = if (uiState.isOnTrack) "On Track" else "Needs Attention",
                                    color = if (uiState.isOnTrack) IncomeGreen else AccentOrange,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (uiState.isOnTrack)
                                        "You are making good progress toward your goal."
                                    else
                                        "Save LKR ${"%,.0f".format(uiState.monthlyTargetNeeded)} per month to reach your goal.",
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
                                text = "LKR ${"%,.0f".format(goal.targetAmount - goal.currentSaved)}",
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
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