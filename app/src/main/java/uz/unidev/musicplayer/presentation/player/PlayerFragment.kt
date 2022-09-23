package uz.unidev.musicplayer.presentation.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.data.models.Music
import uz.unidev.musicplayer.data.models.setSongPosition
import uz.unidev.musicplayer.databinding.FragmentPlayerBinding
import uz.unidev.musicplayer.service.MusicService
import uz.unidev.musicplayer.utils.extensions.formatDuration

/**
 *  Created by Nurlibay Koshkinbaev on 22/09/2022 23:16
 */

class PlayerFragment : Fragment(R.layout.fragment_player), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicList: Array<Music>
        var songPosition = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentPlayerBinding
        var repeat: Boolean = false
    }

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
        binding = FragmentPlayerBinding.bind(view)
        navArgsData()
        if (navArgs.position == 0) musicList.shuffle()
        setMusicData()
        prevPlayNextButtonClicks()
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
        binding.btnRepeat.setOnClickListener {
            if(!repeat){
                repeat = true
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purple_500))
            } else {
                repeat = false
                binding.btnRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_pink))
            }
        }
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
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
            musicService?.showNotification(R.drawable.ic_pause)
            binding.tvStartTime.text = formatDuration(musicService?.mediaPlayer!!.currentPosition.toLong())
            binding.tvEndTime.text = formatDuration(musicService?.mediaPlayer!!.duration.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService?.mediaPlayer!!.duration
            musicService?.mediaPlayer!!.setOnCompletionListener(this)
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
        if(repeat) binding.btnRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purple_500))

    }

    private fun playMusic() {
        binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
        musicService?.showNotification(R.drawable.ic_pause)
        isPlaying = true
        musicService?.mediaPlayer?.start()
    }

    private fun pauseMusic() {
        binding.btnPlayPause.setIconResource(R.drawable.ic_play)
        musicService?.showNotification(R.drawable.ic_play)
        isPlaying = false
        musicService?.mediaPlayer?.pause()
    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
        } else {
            setSongPosition(increment = false)
        }
        setMusicData()
        createMediaPlayer()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setMusicData()
        } catch (e: Exception){
            return
        }
    }
}