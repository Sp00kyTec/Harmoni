// app/src/main/java/com/harmoni/AudioPlayerActivity.kt
package com.harmoni

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.StyledPlayerView

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: StyledPlayerView
    private var serviceBound = false
    private var audioService: AudioService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as AudioService.LocalBinder
            audioService = localBinder.getService()
            serviceBound = true
            updateUIWithPlayerState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            audioService = null
        }
    }

    companion object {
        const val EXTRA_AUDIO_PATH = "audio_path"
        const val EXTRA_TITLE = "title"
        const val EXTRA_ARTIST = "artist"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        playerView = findViewById(R.id.player_view)

        val path = intent.getStringExtra(EXTRA_AUDIO_PATH)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Unknown Title"
        val artist = intent.getStringExtra(EXTRA_ARTIST) ?: "Unknown Artist"

        if (path == null) {
            finish()
            return
        }

        // Update UI immediately
        findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.text_title).text = title
        findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.text_artist).text = artist

        // Start Foreground Service
        val serviceIntent = Intent(this, AudioService::class.java)
        serviceIntent.action = "PLAY"
        serviceIntent.putExtra("path", path)
        serviceIntent.putExtra("title", title)
        serviceIntent.putExtra("artist", artist)

        ContextCompat.startForegroundService(this, serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun updateUIWithPlayerState() {
        val player = audioService?.getPlayer()
        if (player != null) {
            playerView.player = player
        } else {
            finish() // fallback if player failed
        }
    }

    override fun onPause() {
        super.onPause()
        playerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        playerView.onResume()
    }

    override fun onDestroy() {
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
        super.onDestroy()
    }
}