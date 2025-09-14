// app/src/main/java/com/harmoni/AudioPlayerActivity.kt
package com.harmoni

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: StyledPlayerView
    private var exoPlayer: ExoPlayer? = null

    private lateinit var textTitle: androidx.appcompat.widget.AppCompatTextView
    private lateinit var textArtist: androidx.appcompat.widget.AppCompatTextView

    companion object {
        const val EXTRA_AUDIO_PATH = "audio_path"
        const val EXTRA_TITLE = "title"
        const val EXTRA_ARTIST = "artist"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        bindViews()
        setupPlayer()
    }

    private fun bindViews() {
        playerView = findViewById(R.id.player_view)
        textTitle = findViewById(R.id.text_title)
        textArtist = findViewById(R.id.text_artist)
    }

    private fun setupPlayer() {
        val path = intent.getStringExtra(EXTRA_AUDIO_PATH)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Unknown Title"
        val artist = intent.getStringExtra(EXTRA_ARTIST) ?: "Unknown Artist"

        if (path == null) {
            finish()
            return
        }

        // Set UI
        textTitle.text = title
        textArtist.text = artist

        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        val uri = Uri.parse(path)
        val mediaItem = MediaItem.fromUri(uri)

        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.playWhenReady = false
        exoPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}