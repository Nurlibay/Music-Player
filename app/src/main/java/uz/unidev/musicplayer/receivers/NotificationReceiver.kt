package uz.unidev.musicplayer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.data.models.exitApplication
import uz.unidev.musicplayer.data.models.setSongPosition
import uz.unidev.musicplayer.presentation.player.PlayerFragment
import uz.unidev.musicplayer.utils.Constants.EXIT
import uz.unidev.musicplayer.utils.Constants.NEXT
import uz.unidev.musicplayer.utils.Constants.PLAY
import uz.unidev.musicplayer.utils.Constants.PREVIOUS

/**
 *  Created by Nurlibay Koshkinbaev on 23/09/2022 21:51
 */

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            PREVIOUS -> {
                prevNextSong(increment = false, context = context!!)
            }
            PLAY -> {
                if (PlayerFragment.isPlaying) {
                    pauseMusic()
                } else {
                    playMusic()
                }
            }
            NEXT -> {
                prevNextSong(increment = true, context = context!!)
            }
            EXIT -> {
                exitApplication()
            }
        }
    }

    private fun playMusic() {
        PlayerFragment.isPlaying = true
        PlayerFragment.musicService?.mediaPlayer?.start()
        PlayerFragment.musicService?.showNotification(R.drawable.ic_pause)
        PlayerFragment.binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
    }

    private fun pauseMusic() {
        PlayerFragment.isPlaying = false
        PlayerFragment.musicService?.mediaPlayer?.pause()
        PlayerFragment.musicService?.showNotification(R.drawable.ic_play)
        PlayerFragment.binding.btnPlayPause.setIconResource(R.drawable.ic_play)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerFragment.musicService?.createMediaPlayer()
        Glide.with(context)
            .load(PlayerFragment.musicList[PlayerFragment.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop())
            .into(PlayerFragment.binding.ivMusicImage)

        PlayerFragment.binding.tvSongName.text =
            PlayerFragment.musicList[PlayerFragment.songPosition].title
        playMusic()
    }
}