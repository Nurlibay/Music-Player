package uz.unidev.musicplayer.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.app.App
import uz.unidev.musicplayer.broadcast.NotificationReceiver
import uz.unidev.musicplayer.presentation.player.PlayerFragment
import uz.unidev.musicplayer.utils.extensions.formatDuration
import uz.unidev.musicplayer.utils.extensions.getImageArt

/**
 *  Created by Nurlibay Koshkinbaev on 23/09/2022 18:10
 */

class MusicService : Service() {

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    fun showNotification(playPauseBtn: Int){

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(App.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(App.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(App.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(App.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val imgArt = getImageArt(PlayerFragment.musicList[PlayerFragment.songPosition].path)
        val image = if(imgArt != null){
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon)
        }

        val notification = NotificationCompat.Builder(baseContext, App.CHANNEL_ID)
            .setContentTitle(PlayerFragment.musicList[PlayerFragment.songPosition].title)
            .setContentText(PlayerFragment.musicList[PlayerFragment.songPosition].artist)
            .setSmallIcon(R.drawable.ic_music)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_previous, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
            .addAction(R.drawable.ic_exit, "Exit", exitPendingIntent)
            .build()

        startForeground(13, notification)
    }

    fun createMediaPlayer() {
        try {
            if (PlayerFragment.musicService?.mediaPlayer == null) {
                PlayerFragment.musicService?.mediaPlayer = MediaPlayer()
            }
            PlayerFragment.musicService?.mediaPlayer?.reset()
            PlayerFragment.musicService?.mediaPlayer?.setDataSource(PlayerFragment.musicList[PlayerFragment.songPosition].path)
            PlayerFragment.musicService?.mediaPlayer?.prepare()
            PlayerFragment.binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
            PlayerFragment.musicService?.showNotification(R.drawable.ic_pause)
            PlayerFragment.binding.tvStartTime.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerFragment.binding.tvEndTime.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerFragment.binding.seekBar.progress = 0
            PlayerFragment.binding.seekBar.max = mediaPlayer!!.duration
        } catch (e: Exception) {
            return
        }
    }

    fun seekBarSetup(){
        runnable = Runnable {
            PlayerFragment.binding.tvStartTime.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerFragment.binding.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }
}