package com.nammamistri.database

import androidx.lifecycle.LiveData
import androidx.room.*

// DAO = Data Access Object = the "menu" of database operations
@Dao
interface WorkerDao {

    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): LiveData<List<Worker>>

    @Insert
    suspend fun insertWorker(worker: Worker)

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)
}