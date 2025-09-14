// app/src/main/java/com/harmoni/RecentlyPlayed.kt
package com.harmoni

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayed(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaPath: String,
    val title: String,
    val artist: String?,
    val type: String, // "audio" or "video"
    val playedAt: Long = System.currentTimeMillis()
)