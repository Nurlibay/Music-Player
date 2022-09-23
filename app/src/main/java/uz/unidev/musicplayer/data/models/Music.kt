package uz.unidev.musicplayer.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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