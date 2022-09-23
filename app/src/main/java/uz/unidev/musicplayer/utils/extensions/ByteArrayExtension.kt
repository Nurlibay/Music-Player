package uz.unidev.musicplayer.utils.extensions

import android.media.MediaMetadataRetriever

fun getImageArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}