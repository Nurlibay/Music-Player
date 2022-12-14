package uz.unidev.musicplayer.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import uz.unidev.musicplayer.presentation.player.PlayerFragment
import kotlin.system.exitProcess

/**
 *  Created by Nurlibay Koshkinbaev on 23/09/2022 12:37
 */

@Parcelize
data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String,
    val artUri: String
): Parcelable

fun setSongPosition(increment: Boolean) {
    if(!PlayerFragment.repeat){
        if (increment) {
            if (PlayerFragment.musicList.size - 1 == PlayerFragment.songPosition) {
                PlayerFragment.songPosition = 0
            } else {
                ++PlayerFragment.songPosition
            }
        } else {
            if (PlayerFragment.songPosition == 0) {
                PlayerFragment.songPosition = PlayerFragment.musicList.size - 1
            } else {
                --PlayerFragment.songPosition
            }
        }
    }
}

fun exitApplication(){
    if(PlayerFragment.musicService != null) {
        PlayerFragment.musicService!!.stopForeground(true)
        PlayerFragment.musicService!!.mediaPlayer!!.release()
        PlayerFragment.musicService = null
    }
    exitProcess(1)
}