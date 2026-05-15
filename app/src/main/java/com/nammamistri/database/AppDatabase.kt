package com.nammamistri.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// List all your tables here in entities = []
@Database(
    entities = [Worker::class, MaterialRate::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workerDao(): WorkerDao
    abstract fun materialRateDao(): MaterialRateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Call this anywhere in the app to get the database
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "namma_mistri_db"  // file name of the database
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}