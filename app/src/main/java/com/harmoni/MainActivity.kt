// app/src/main/java/com/harmoni/MainActivity.kt
package com.harmoni

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.harmoni.fragment.AudioFragment
import com.harmoni.fragment.VideosFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_READ_STORAGE = 100

        var videoList = listOf<Video>()
        var audioList = listOf<Audio>()
    }

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    // Mini Player Views
    private lateinit var miniPlayer: LinearLayout
    private lateinit var textMiniTitle: TextView
    private lateinit var textMiniArtist: TextView
    private lateinit var btnPlayPause: ImageButton

    // Service Binding
    private var serviceBound = false
    private var audioService: AudioService? = null
    private var currentPlayingAudio: Audio? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as AudioService.LocalBinder
            audioService = localBinder.getService()
            serviceBound = true
            updateMiniPlayerState()
            setupMiniPlayerClicks()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            audioService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "audio_playback_channel",
                "Background Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows controls when playing audio in the background"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        bindViews()
        setupClickListeners()
        setupTabs()

        // Bind to AudioService if running
        val intent = Intent(this, AudioService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)

        requestStoragePermission()
    }

    private fun bindViews() {
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)

        // Mini Player
        miniPlayer = findViewById(R.id.mini_player_container)
        textMiniTitle = findViewById(R.id.text_mini_title)
        textMiniArtist = findViewById(R.id.text_mini_artist)
        btnPlayPause = findViewById(R.id.btn_play_pause)
    }

    private fun setupClickListeners() {
        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        miniPlayer.setOnClickListener {
            val intent = Intent(this, AudioPlayerActivity::class.java).apply {
                putExtra(AudioPlayerActivity.EXTRA_AUDIO_PATH, currentPlayingAudio?.path)
                putExtra(AudioPlayerActivity.EXTRA_TITLE, currentPlayingAudio?.title)
                putExtra(AudioPlayerActivity.EXTRA_ARTIST, currentPlayingAudio?.artist)
            }
            startActivity(intent)
        }
    }

    private fun togglePlayPause() {
        if (!serviceBound) return

        val player = audioService?.getPlayer() ?: return
        if (player.playWhenReady) {
            player.playWhenReady = false
            btnPlayPause.setImageResource(R.drawable.ic_play)
        } else {
            player.playWhenReady = true
            btnPlayPause.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun setupTabs() {
        val adapter = MainTabsAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Videos"
                1 -> tab.text = "Audio"
            }
        }.attach()
    }

    private fun requestStoragePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                scanMedia()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_STORAGE
                )
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_STORAGE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanMedia()
            }
        }
    }

    private fun scanMedia() {
        Thread {
            videoList = scanVideos()
            audioList = scanAudios()

            runOnUiThread {
                Toast.makeText(
                    this,
                    "Found ${videoList.size} videos, ${audioList.size} songs",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.start()
    }

    // --- Data Classes ---
    data class Video(
        val id: Long,
        override val title: String,
        val displayName: String?,
        val duration: Int,
        val size: Long,
        override val path: String
    ) : MediaItem {
        override val subtitle: String
            get() = "Duration: ${formatDuration(duration)} • ${formatSize(size)}"
    }

    data class Audio(
        val id: Long,
        override val title: String,
        val artist: String?,
        val album: String?,
        val duration: Int,
        val size: Long,
        override val path: String
    ) : MediaItem {
        override val subtitle: String
            get() = "by ${artist ?: "Unknown Artist"} • ${formatDuration(duration)}"
    }

    // --- Scanning Functions ---
    private fun scanVideos(): List<Video> {
        val videos = mutableListOf<Video>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA
        )

        val selection = "${MediaStore.Video.Media.SIZE} > ?"
        val selectionArgs = arrayOf("1024")
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val cols = getIndices(cursor)
            while (cursor.moveToNext()) {
                videos.add(
                    Video(
                        id = cursor.getLong(cols["id"]!!),
                        title = cursor.getString(cols["title"]!!),
                        displayName = cursor.getString(cols["displayName"]),
                        duration = cursor.getInt(cols["duration"]!!),
                        size = cursor.getLong(cols["size"]!!),
                        path = cursor.getString(cols["path"]!!)
                    )
                )
            }
        }
        return videos
    }

    private fun scanAudios(): List<Audio> {
        val audios = mutableListOf<Audio>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = ? AND ${MediaStore.Audio.Media.SIZE} > ?"
        val selectionArgs = arrayOf("1", "1024")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val cols = getIndices(cursor)
            while (cursor.moveToNext()) {
                audios.add(
                    Audio(
                        id = cursor.getLong(cols["id"]!!),
                        title = cursor.getString(cols["title"]!!),
                        artist = cursor.getString(cols["artist"]),
                        album = cursor.getString(cols["album"]),
                        duration = cursor.getInt(cols["duration"]!!),
                        size = cursor.getLong(cols["size"]!!),
                        path = cursor.getString(cols["path"]!!)
                    )
                )
            }
        }
        return audios
    }

    private fun getIndices(cursor: Cursor): Map<String, Int> {
        return mapOf(
            "id" to cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID),
            "title" to cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE),
            "displayName" to cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME) ?: -1,
            "duration" to cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION),
            "size" to cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE),
            "path" to cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA),
            "artist" to cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST) ?: -1,
            "album" to cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM) ?: -1
        )
    }

    // Utility functions
    private fun formatDuration(ms: Int): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / 1000 / 60) % 60
        val hours = (ms / 1000 / 3600)
        return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else String.format("%02d:%02d", minutes, seconds)
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
            bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0 / 1024)} MB"
            else -> "${"%.1f".format(bytes / 1024.0 / 1024 / 1024)} GB"
        }
    }

    // --- Mini Player Logic ---
    fun updateMiniPlayerState() {
        if (!serviceBound) {
            miniPlayer.visibility = LinearLayout.GONE
            return
        }

        val player = audioService?.getPlayer() ?: run {
            miniPlayer.visibility = LinearLayout.GONE
            return
        }

        // Try to find which audio is playing
        val playingPath = getCurrentPlayingPath(player)
        currentPlayingAudio = audioList.find { it.path == playingPath }

        if (currentPlayingAudio != null) {
            textMiniTitle.text = currentPlayingAudio?.title
            textMiniArtist.text = currentPlayingAudio?.artist ?: "Unknown Artist"
            miniPlayer.visibility = LinearLayout.VISIBLE
            btnPlayPause.setImageResource(
                if (player.playWhenReady) R.drawable.ic_pause else R.drawable.ic_play
            )
        } else {
            miniPlayer.visibility = LinearLayout.GONE
        }
    }

    private fun getCurrentPlayingPath(player: Player): String? {
        return try {
            val mediaItem = player.currentMediaItem ?: return null
            mediaItem.localConfiguration?.uri.toString()
        } catch (e: Exception) {
            null
        }
    }

    override fun onResume() {
        super.onResume()
        updateMiniPlayerState()
    }

    override fun onDestroy() {
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
        super.onDestroy()
    }
}