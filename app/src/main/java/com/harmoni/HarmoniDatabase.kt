// app/src/main/java/com/harmoni/HarmoniDatabase.kt
package com.harmoni

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [Playlist::class, PlaylistEntry::class],
    version = 1,
    exportSchema = false
)
abstract class HarmoniDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: HarmoniDatabase? = null

        fun getDatabase(context: Context): HarmoniDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HarmoniDatabase::class.java,
                    "harmoni_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}