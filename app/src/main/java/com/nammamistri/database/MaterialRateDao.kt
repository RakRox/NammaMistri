package com.nammamistri.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MaterialRateDao {

    @Query("SELECT * FROM material_rates ORDER BY materialName ASC")
    fun getAllRates(): LiveData<List<MaterialRate>>

    @Query("SELECT * FROM material_rates ORDER BY materialName ASC")
    suspend fun getAllRatesOnce(): List<MaterialRate>

    // NEW: count rows — used to safely add default rates only once
    @Query("SELECT COUNT(*) FROM material_rates")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: MaterialRate)

    @Update
    suspend fun updateRate(rate: MaterialRate)

    @Delete
    suspend fun deleteRate(rate: MaterialRate)
}