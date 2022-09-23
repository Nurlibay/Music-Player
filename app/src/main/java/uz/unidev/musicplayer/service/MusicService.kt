package uz.unidev.musicplayer.service

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.app.App
import uz.unidev.musicplayer.data.models.Music

/**
 *  Created by Nurlibay Koshkinbaev on 23/09/2022 18:10
 */

class MusicService : Service() {

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    fun showNotification(musicList: Array<Music>, pos: Int){
        val notification = NotificationCompat.Builder(baseContext, App.CHANNEL_ID)
            .setContentTitle(musicList[pos].title)
            .setContentText(musicList[pos].artist)
            .setSmallIcon(R.drawable.ic_music)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.music_player_icon))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_previous, "Previous", null)
            .addAction(R.drawable.ic_play, "Play", null)
            .addAction(R.drawable.ic_next, "Next", null)
            .addAction(R.drawable.ic_exit, "Exit", null)
            .build()

        startForeground(13, notification)
    }
}