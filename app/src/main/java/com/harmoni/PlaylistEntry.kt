package com.harmoni

import androidx.room.*

@Entity(
    tableName = "playlist_entries",
    primaryKeys = ["playlistId", "audioPath"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId")]
)
data class PlaylistEntry(
    val playlistId: Long,
    val audioPath: String
)