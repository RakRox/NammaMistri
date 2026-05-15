package com.nammamistri.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "material_rates")
data class MaterialRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val materialName: String,   // e.g. "Cement"
    val unit: String,           // e.g. "bag", "load", "1000 nos"
    val pricePerUnit: Double    // e.g. 350.0
)