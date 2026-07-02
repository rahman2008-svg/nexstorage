package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DbFolder::class, DbFile::class, UserSession::class],
    version = 1,
    exportSchema = false
)
abstract class StorageDatabase : RoomDatabase() {

    abstract fun storageDao(): StorageDao

    companion object {
        @Volatile
        private var INSTANCE: StorageDatabase? = null

        fun getDatabase(context: Context): StorageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StorageDatabase::class.java,
                    "nexstorage_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
