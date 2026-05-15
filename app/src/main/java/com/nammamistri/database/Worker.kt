package com.nammamistri.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity means this class = one table in our database
@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,           // Worker's name
    val dailyWage: Double,      // How much they earn per day
    val totalDays: Int = 0,     // Days worked
    val totalAdvance: Double = 0.0  // Total advance money given
)