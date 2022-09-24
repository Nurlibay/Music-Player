package uz.unidev.musicplayer.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.data.models.Music
import uz.unidev.musicplayer.data.models.exitApplication
import uz.unidev.musicplayer.databinding.FragmentMainBinding
import uz.unidev.musicplayer.presentation.player.PlayerFragment
import uz.unidev.musicplayer.utils.Constants.SPEECH_REQUEST_CODE
import java.io.File

/**
 *  Created by Nurlibay Koshkinbaev on 22/09/2022 19:07
 */

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding: FragmentMainBinding by viewBinding()
    private val navController: NavController by lazy { findNavController() }
    private val adapter: MusicAdapter by lazy { MusicAdapter() }
    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

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
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            val query = text.toString().lowercase()
            adapter.query = text.toString()
            filterWithQuery(query)
            toggleImageView(query)
        }

        binding.clearSearchQuery.setOnClickListener {
            binding.searchEditText.setText("")
            binding.noSearchResultsFoundText.visibility = View.INVISIBLE
        }

        binding.voiceSearchQuery.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
            }
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
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

    private fun filterWithQuery(query: String) {
        val filteredList: ArrayList<Music> = ArrayList()
        if (query.isNotEmpty()) {
//            handler.postDelayed({
//                for (item in musicList) {
//                    Timber.d("TTT", musicList.size.toString())
//                    if (item.title.lowercase().contains(query)) {
//                        filteredList.add(item)
//                    }
//                }
//            }, 300)
            for (item in musicList) {
                Timber.d("TTT", musicList.size.toString())
                if (item.title.lowercase().contains(query)) {
                    filteredList.add(item)
                }
            }
            if(filteredList.isEmpty()){
                binding.noSearchResultsFoundText.visibility = View.VISIBLE
            } else {
                binding.noSearchResultsFoundText.visibility = View.INVISIBLE
            }
            adapter.musicList = filteredList
        } else {
            adapter.musicList = musicList
        }
    }

    private fun toggleImageView(query: String) {
        if (query.isNotEmpty()) {
            binding.clearSearchQuery.visibility = View.VISIBLE
            binding.voiceSearchQuery.visibility = View.INVISIBLE
        } else if (query.isEmpty()) {
            binding.clearSearchQuery.visibility = View.INVISIBLE
            binding.voiceSearchQuery.visibility = View.VISIBLE
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
                    val builder = MaterialAlertDialogBuilder(requireContext())
                    builder.setTitle("Exit")
                        .setMessage("Do you want to close app?")
                        .setPositiveButton("YES"){ _, _ ->
                            exitApplication()
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
            //menuItem.isChecked = true
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

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerFragment.isPlaying && PlayerFragment.musicService != null){
            exitApplication()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results?.get(0)
                }
            binding.searchEditText.setText(spokenText)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}