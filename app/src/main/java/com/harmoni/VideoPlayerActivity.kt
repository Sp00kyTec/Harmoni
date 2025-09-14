// app/src/main/java/com/harmoni/VideoPlayerActivity.kt
package com.harmoni

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: StyledPlayerView
    private var exoPlayer: ExoPlayer? = null

    companion object {
        const val EXTRA_VIDEO_PATH = "video_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        playerView = findViewById(R.id.player_view)

        val videoPath = intent.getStringExtra(EXTRA_VIDEO_PATH)
        if (videoPath == null) {
            finish()
            return
        }

        initializePlayer(videoPath)
    }

    private fun initializePlayer(videoPath: String) {
        // Create ExoPlayer instance
        exoPlayer = ExoPlayer.Builder(this).build()

        // Bind player to view
        playerView.player = exoPlayer

        // Create media item from file path
        val uri = Uri.parse(videoPath)
        val mediaItem = MediaItem.fromUri(uri)

        // Set media and start loading
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true  // auto-play

        // Optional: keep screen on during playback
        playerView.keepScreenOn = true
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onStop() {
        super.onStop()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            exoPlayer?.playWhenReady = false
        }
        exoPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
        playerView.keepScreenOn = false
    }
}