package uz.unidev.musicplayer.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.data.models.Music
import uz.unidev.musicplayer.databinding.FragmentMainBinding
import java.io.File

/**
 *  Created by Nurlibay Koshkinbaev on 22/09/2022 19:07
 */

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding: FragmentMainBinding by viewBinding()
    private val navController: NavController by lazy { findNavController() }
    private val adapter: MusicAdapter by lazy { MusicAdapter() }

    companion object {
        lateinit var musicList: ArrayList<Music>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRuntimePermission()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            drawer()
            initRV()
            navigate()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun FragmentMainBinding.initRV() {
        rvSongs.adapter = adapter
        rvSongs.setHasFixedSize(true)
        rvSongs.setItemViewCacheSize(13)
        musicList = loadAllMusicsFromStorage()
        adapter.musicList = musicList

        tvTotalSongsCounts.text = "Total songs: ${musicList.size}"

        adapter.onItemClickListener { pos, musics ->
            navController.navigate(MainFragmentDirections.actionMainFragmentToPlayerFragment(musics.toTypedArray(), pos))
        }
    }

    private fun FragmentMainBinding.navigate() {
        cardShuffle.setOnClickListener {
            navController.navigate(MainFragmentDirections.actionMainFragmentToPlayerFragment(musicList.toTypedArray(), 0))
        }
        cardFavourite.setOnClickListener {
            navController.navigate(MainFragmentDirections.actionMainFragmentToFavouriteFragment())
        }
        cardPlaylist.setOnClickListener {
            navController.navigate(MainFragmentDirections.actionMainFragmentToPlaylistFragment())
        }
    }

    private fun FragmentMainBinding.drawer() {
        ivMenu.setOnClickListener {
            drawerLayout.open()
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navFeedback -> {
                    navController.navigate(R.id.feedbackFragment)
                }
                R.id.navAbout -> {
                    Toast.makeText(requireContext(), "About", Toast.LENGTH_SHORT).show()
                }
                R.id.navExit -> {
                    requireActivity().finish()
                }
            }
            menuItem.isChecked = true
            drawerLayout.close()
            true
        }
    }

    private fun loadAllMusicsFromStorage(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = requireActivity().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(id = idC, title = titleC, album = albumC, artist = artistC, duration = durationC, path = pathC, artUriC)
                    val file = File(music.path)
                    if (file.exists()) {
                        tempList.add(music)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
    }

    private fun requestRuntimePermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 13
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 13
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.initRV()
    }
}