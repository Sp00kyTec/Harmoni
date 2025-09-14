// app/src/main/java/com/harmoni/PlaylistDao.kt
package com.harmoni

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    // Entries
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(entry: PlaylistEntry): Long

    @Delete
    suspend fun removeTrackFromPlaylist(entry: PlaylistEntry)

    @Query("SELECT * FROM playlist_entries WHERE playlistId = :playlistId")
    suspend fun getTracksInPlaylist(playlistId: Long): List<PlaylistEntry>

    @Query("SELECT COUNT(*) FROM playlist_entries WHERE playlistId = :playlistId AND audioPath = :audioPath")
    suspend fun isInPlaylist(playlistId: Long, audioPath: String): Int
}