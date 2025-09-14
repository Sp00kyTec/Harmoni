package com.harmoni

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.Util
import java.util.*

class AudioService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private var sleepTimerJob: Timer? = null

    // Equalizer
    var equalizer: Equalizer? = null
        private set

    // Binder
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    // Headset plug receiver
    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", 0)
                if (state == 0 && exoPlayer?.playWhenReady == true) {
                    exoPlayer?.playWhenReady = false
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Register headset receiver
        registerReceiver(headsetReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))

        // Setup Media Session
        mediaSession = MediaSessionCompat(this, "AudioService")
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession.isActive = true

        // Create ExoPlayer with gapless support
        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setUseLazyPreparation(true)
            .build()

        // Attach equalizer
        setupEqualizer()

        // Notification Manager
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            1001,
            "audio_playback_channel"
        ).also { builder ->
            builder.setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return intent?.getStringExtra("title") ?: "Unknown Title"
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? = null

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return intent?.getStringExtra("artist") ?: "Unknown Artist"
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Any? = null
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

    private fun setupEqualizer() {
        val player = exoPlayer ?: return
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && player.playWhenReady) {
                    val sessionId = player.audioSessionId
                    if (sessionId != C.AUDIO_SESSION_ID_UNSET) {
                        createEqualizer(sessionId)
                    }
                }
            }
        })
    }

    private fun createEqualizer(sessionId: Int) {
        try {
            val eq = Equalizer(0, sessionId.toShort())
            eq.enabled = true

            // Restore saved settings
            val sharedPrefs = getSharedPreferences("equalizer", Context.MODE_PRIVATE)
            val numBands = eq.numberOfBands
            for (i in 0 until numBands) {
                val level = sharedPrefs.getInt("band_$i", -1)
                if (level != -1) {
                    eq.setBandLevel(i.toShort(), level.toShort())
                }
            }
            equalizer = eq
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY" -> {
                val path = intent.getStringExtra("path")
                if (path != null) {
                    playAudio(path)
                }
            }
            "PAUSE" -> {
                exoPlayer?.playWhenReady = false
            }
            "STOP" -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            "SLEEP_TIMER" -> {
                val minutes = intent.getIntExtra("minutes", 5)
                setupSleepTimer(minutes)
            }
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

    private fun setupSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                stopSelf()
            }
        }, minutes * 60_000L)
        sleepTimerJob = timer
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        sleepTimerJob?.cancel()

        equalizer?.enabled = false
        equalizer?.release()
        equalizer = null

        mediaSession.release()
        playerNotificationManager.setPlayer(null)
        exoPlayer?.release()
        exoPlayer = null

        unregisterReceiver(headsetReceiver)
        super.onDestroy()
    }
}