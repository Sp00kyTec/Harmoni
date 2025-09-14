package com.harmoni

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Playlist::class, PlaylistEntry::class, RecentlyPlayed::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HarmoniDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao

    companion object {
        @Volatile
        private var INSTANCE: HarmoniDatabase? = null

        // Incremented version with migration
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS recently_played (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        mediaPath TEXT NOT NULL,
                        title TEXT NOT NULL,
                        artist TEXT,
                        type TEXT NOT NULL,
                        playedAt INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getDatabase(context: Context): HarmoniDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HarmoniDatabase::class.java,
                    "harmoni_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // optional: remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Converters for Room
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

// DAOs
@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): androidx.room.Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(entry: PlaylistEntry): Long

    @Delete
    suspend fun removeTrackFromPlaylist(entry: PlaylistEntry)

    @Query("SELECT * FROM playlist_entries WHERE playlistId = :playlistId")
    suspend fun getTracksInPlaylist(playlistId: Long): List<PlaylistEntry>

    @Query("SELECT COUNT(*) FROM playlist_entries WHERE playlistId = :playlistId AND audioPath = :audioPath")
    suspend fun isInPlaylist(playlistId: Long, audioPath: String): Int

    @Transaction
    @Query("""
        SELECT p.*, COUNT(pe.audioPath) as trackCount 
        FROM playlists p 
        LEFT JOIN playlist_entries pe ON p.id = pe.playlistId 
        GROUP BY p.id 
        ORDER BY p.createdAt DESC
    """)
    fun getPlaylistsWithCounts(): androidx.room.Flow<List<PlaylistWithCount>>
}

@Dao
interface RecentlyPlayedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecentlyPlayed)

    @Query("SELECT * FROM recently_played WHERE type = :type ORDER BY playedAt DESC LIMIT 20")
    fun getRecentAudio(): androidx.room.Flow<List<RecentlyPlayed>>

    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT 20")
    fun getRecent(): androidx.room.Flow<List<RecentlyPlayed>>
}

// Data classes
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

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

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val trackCount: Int
)

@Entity(tableName = "recently_played")
data class RecentlyPlayed(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaPath: String,
    val title: String,
    val artist: String?,
    val type: String, // "audio" or "video"
    val playedAt: Long = System.currentTimeMillis()
)