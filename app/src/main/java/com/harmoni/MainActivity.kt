// app/src/main/java/com/harmoni/MainActivity.kt
package com.harmoni

import android.Manifest
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnScanMedia: Button
    private lateinit var textStatus: TextView

    companion object {
        private const val REQUEST_READ_STORAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupClickListeners()
        updateStatus("Ready to scan your media library")
    }

    private fun bindViews() {
        btnScanMedia = findViewById(R.id.btn_scan_media)
        textStatus = findViewById(R.id.text_status)
    }

    private fun setupClickListeners() {
        btnScanMedia.setOnClickListener {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                onPermissionGranted()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                showPermissionRationale()
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

    private fun showPermissionRationale() {
        Toast.makeText(
            this,
            "Harmoni needs access to storage to play your videos and music.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                updateStatus("❌ Storage access denied")
                Toast.makeText(this, "Cannot access media without permission.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onPermissionGranted() {
        updateStatus("🔍 Scanning videos and music...")
        Thread {
            val videos = scanVideos()
            val audios = scanAudios()

            runOnUiThread {
                val message = """
                    ✅ Found:
                    🎞️ ${videos.size} Videos
                    🎵 ${audios.size} Audio Files
                    
                    Tap 'Scan' again to refresh.
                """.trimIndent()
                updateStatus(message)

                Toast.makeText(this, "Scan complete!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    // Data class to hold video info
    data class Video(
        val id: Long,
        val title: String,
        val displayName: String?,
        val duration: Int, // milliseconds
        val size: Long, // bytes
        val path: String
    )

    // Data class to hold audio info
    data class Audio(
        val id: Long,
        val title: String,
        val artist: String?,
        val album: String?,
        val duration: Int,
        val size: Long,
        val path: String
    )

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
            MediaStore.Video.Media.DATA // deprecated but still used for path
        )

        val selection = "${MediaStore.Video.Media.SIZE} > ?"
        val selectionArgs = arrayOf("1024") // larger than 1KB
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val displayName = cursor.getString(displayNameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(dataColumn)

                videos.add(
                    Video(
                        id = id,
                        title = title,
                        displayName = displayName,
                        duration = duration,
                        size = size,
                        path = path
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
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(dataColumn)

                audios.add(
                    Audio(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        size = size,
                        path = path
                    )
                )
            }
        }
        return audios
    }

    private fun updateStatus(text: String) {
        textStatus.text = text
    }
}