package uz.unidev.musicplayer.presentation.player

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.data.models.Music
import uz.unidev.musicplayer.data.models.exitApplication
import uz.unidev.musicplayer.data.models.setSongPosition
import uz.unidev.musicplayer.databinding.FragmentPlayerBinding
import uz.unidev.musicplayer.service.MusicService
import uz.unidev.musicplayer.utils.extensions.formatDuration
import uz.unidev.musicplayer.utils.extensions.showMessage
import kotlin.system.exitProcess

/**
 *  Created by Nurlibay Koshkinbaev on 22/09/2022 23:16
 */

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicList: Array<Music>
        var songPosition = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentPlayerBinding
        var repeat: Boolean = false

        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
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

        binding.btnEqualizer.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            } catch (e: Exception){
                showMessage("Equaliser Feature not Supported!!")
            }
        }

        binding.btnTimer.setOnClickListener {
            val timer = min15 || min30 || min60
            if(!timer) {
                showBottomSheetDialog()
            } else {
                val builder = MaterialAlertDialogBuilder(requireContext())
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop timer?")
                    .setPositiveButton("YES"){ _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.btnTimer.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_pink))
                    }
                    .setNegativeButton("NO"){ dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }

        binding .btnShare.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicList[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Share Song File!!"))
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

        if(min15 || min30 || min60) {
            binding.btnTimer.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purple_500))
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 13 && resultCode == RESULT_OK){
            return
        }
    }

    private fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            showMessage("Music will stop after 15 minutes")
            binding.btnTimer.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purple_500))
            min15 = true
            Thread {
                Thread.sleep(15 * 60000)
                if(min15){
                    exitApplication()
                }
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            showMessage("Music will stop after 30 minutes")
            binding.btnTimer.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purple_500))
            min30 = true
            Thread {
                Thread.sleep(30 * 60000)
                if(min30){
                    exitApplication()
                }
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            showMessage("Music will stop after 60 minutes")
            binding.btnTimer.setColorFilter(ContextCompat.getColor(requireContext(), R.color.purple_500))
            min60 = true
            Thread {
                Thread.sleep(60 * 60000)
                if(min60){
                    exitApplication()
                }
            }.start()
            dialog.dismiss()
        }
    }
}