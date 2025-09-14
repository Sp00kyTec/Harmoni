// app/src/main/java/com/harmoni/AudioService.kt
package com.harmoni

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.session.MediaSession
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.Util

class AudioService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var playerNotificationManager: PlayerNotificationManager

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()

        // Setup Media Session
        mediaSession = MediaSessionCompat(this, "AudioService")
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.isActive = true

        // Create ExoPlayer
        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        // Notification Manager
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            1001,
            "audio_playback_channel"
        ).also { builder ->
            builder.setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return try {
                        intent?.getStringExtra("title") ?: "Unknown Title"
                    } catch (e: Exception) {
                        "Unknown Title"
                    }
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? = null

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return intent?.getStringExtra("artist") ?: "Unknown Artist"
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? = null
            })
        }.build().apply {
            setPlayer(exoPlayer)
            setUseStopActionInForegroundNotification(true)
            setUseNextAction(false)
            setUsePreviousAction(false)
            setMediaSessionToken(mediaSession.sessionToken)
            setSmallIcon(R.drawable.ic_file)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "PLAY") {
            val path = intent.getStringExtra("path")
            if (path != null) {
                playAudio(path)
            }
        } else if (intent?.action == "PAUSE") {
            exoPlayer?.playWhenReady = false
        } else if (intent?.action == "STOP") {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_STICKY
    }

    private fun playAudio(path: String) {
        val uri = Uri.parse(path)
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    override fun onDestroy() {
        mediaSession.release()
        playerNotificationManager.setPlayer(null)
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }
}