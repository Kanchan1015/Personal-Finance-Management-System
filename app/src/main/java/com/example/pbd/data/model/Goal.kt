package com.example.pbd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val targetAmount: Double = 0.0,
    val currentSaved: Double = 0.0,
    val deadline: Long = 0L,
    val status: String = "ACTIVE"
)