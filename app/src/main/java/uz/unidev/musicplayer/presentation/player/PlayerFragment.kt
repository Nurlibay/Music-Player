package uz.unidev.musicplayer.presentation.player

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.data.models.Music
import uz.unidev.musicplayer.databinding.FragmentPlayerBinding
import uz.unidev.musicplayer.service.MusicService

/**
 *  Created by Nurlibay Koshkinbaev on 22/09/2022 23:16
 */

class PlayerFragment : Fragment(R.layout.fragment_player), ServiceConnection {

    companion object {
        lateinit var musicList: Array<Music>
        var songPosition = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
    }

    private val binding: FragmentPlayerBinding by viewBinding()
    private val navArgs: PlayerFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Starting Service */
        val intent = Intent(requireContext(), MusicService::class.java)
        requireActivity().bindService(intent, this, BIND_AUTO_CREATE)
        requireActivity().startService(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navArgsData()
        if (navArgs.position == 0) musicList.shuffle()
        setMusicData()
        prevPlayNextButtonClicks()
    }

    private fun prevPlayNextButtonClicks() {
        binding.btnPrevious.setOnClickListener {
            prevNextSong(increment = false)
        }

        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        binding.btnNext.setOnClickListener {
            prevNextSong(increment = true)
        }
    }

    private fun navArgsData() {
        songPosition = navArgs.position
        musicList = navArgs.musics
    }

    private fun createMediaPlayer() {
        try {
            if (musicService?.mediaPlayer == null) {
                musicService?.mediaPlayer = MediaPlayer()
            }
            musicService?.mediaPlayer?.reset()
            musicService?.mediaPlayer?.setDataSource(musicList[songPosition].path)
            musicService?.mediaPlayer?.prepare()
            musicService?.mediaPlayer?.start()
            isPlaying = true
            binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
        } catch (e: Exception) {
            return
        }
    }

    private fun setMusicData() {
        Glide.with(binding.root.context)
            .load(musicList[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop())
            .into(binding.ivMusicImage)

        binding.tvSongName.text = musicList[songPosition].title
    }

    private fun playMusic() {
        binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
        isPlaying = true
        musicService?.mediaPlayer?.start()
    }

    private fun pauseMusic() {
        binding.btnPlayPause.setIconResource(R.drawable.ic_play)
        isPlaying = false
        musicService?.mediaPlayer?.pause()
    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            setMusicData()
            createMediaPlayer()
        } else {
            setSongPosition(increment = false)
            setMusicData()
            createMediaPlayer()
        }
    }

    private fun setSongPosition(increment: Boolean) {
        if (increment) {
            if (musicList.size - 1 == songPosition) {
                songPosition = 0
            } else {
                ++songPosition
            }
        } else {
            if (songPosition == 0) {
                songPosition = musicList.size - 1
            } else {
                --songPosition
            }
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService?.showNotification(musicList, songPosition)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}