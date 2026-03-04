package com.example.devinci.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.devinci.R
import com.example.devinci.model.Song
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter
    
    // UI Elements
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var miniPlayer: ConstraintLayout
    private lateinit var fullPlayer: ConstraintLayout
    
    // Mini Player
    private lateinit var tvMiniTitle: TextView
    private lateinit var tvMiniArtist: TextView
    private lateinit var ivMiniArt: ImageView
    private lateinit var btnMiniPlayPause: ImageButton
    
    // Full Player
    private lateinit var tvFullTitle: TextView
    private lateinit var tvFullArtist: TextView
    private lateinit var ivFullArt: ImageView
    private lateinit var btnPlayPause: FloatingActionButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnShuffle: ImageButton
    private lateinit var btnCollapse: ImageButton
    private lateinit var seekBar: SeekBar
    
    // Search
    private lateinit var etSearch: EditText

    private var updateSeekbarJob: Job? = null

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel.loadSongs()
        } else {
            Toast.makeText(this, "Permission denied. Cannot load music.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
        setupBottomSheet()
        setupRecyclerView()
        observeViewModel()
        checkPermissionsAndLoad()
    }

    private fun setupUI() {
        miniPlayer = findViewById(R.id.miniPlayer)
        fullPlayer = findViewById(R.id.fullPlayer)
        
        tvMiniTitle = findViewById(R.id.tvMiniTitle)
        tvMiniArtist = findViewById(R.id.tvMiniArtist)
        ivMiniArt = findViewById(R.id.ivMiniArt)
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause)

        tvFullTitle = findViewById(R.id.tvFullTitle)
        tvFullArtist = findViewById(R.id.tvFullArtist)
        ivFullArt = findViewById(R.id.ivFullArt)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnShuffle = findViewById(R.id.btnShuffle)
        btnCollapse = findViewById(R.id.btnCollapse)
        seekBar = findViewById(R.id.seekBar)
        etSearch = findViewById(R.id.etSearch)

        btnMiniPlayPause.setOnClickListener { viewModel.playPause() }
        btnPlayPause.setOnClickListener { viewModel.playPause() }
        btnNext.setOnClickListener { viewModel.skipToNext() }
        btnPrevious.setOnClickListener { viewModel.skipToPrevious() }
        btnShuffle.setOnClickListener { viewModel.toggleShuffle() }
        btnCollapse.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED }
        
        miniPlayer.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seekTo(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showSongOptions(view: View, song: Song) {
        val popup = PopupMenu(this, view)
        popup.menu.add("Play Next")
        popup.menu.add("Add to Queue")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Play Next" -> {
                    viewModel.playNext(song)
                    Toast.makeText(this, "Will play next: ${song.title}", Toast.LENGTH_SHORT).show()
                }
                "Add to Queue" -> {
                    viewModel.addToQueue(song)
                    Toast.makeText(this, "Added to queue: ${song.title}", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        popup.show()
    }

    private fun setupBottomSheet() {
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        miniPlayer.visibility = View.GONE
                        fullPlayer.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        miniPlayer.visibility = View.VISIBLE
                        fullPlayer.visibility = View.GONE
                    }
                    else -> {}
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                miniPlayer.alpha = 1 - slideOffset
                fullPlayer.alpha = slideOffset
            }
        })
    }

    private fun setupRecyclerView() {
        val rvSongs = findViewById<RecyclerView>(R.id.rvSongs)
        songAdapter = SongAdapter(
            onSongClick = { song -> viewModel.playSong(song) },
            onOptionsClick = { view, song -> showSongOptions(view, song) }
        )
        rvSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.songs.collect { songs ->
                songAdapter.submitList(songs)
            }
        }

        lifecycleScope.launch {
            viewModel.currentSong.collect { song ->
                song?.let {
                    tvMiniTitle.text = it.title
                    tvMiniArtist.text = it.artist
                    tvFullTitle.text = it.title
                    tvFullArtist.text = it.artist
                    seekBar.max = it.duration.toInt()
                    
                    Glide.with(this@MainActivity)
                        .load(it.albumArtUri)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivMiniArt)
                        
                    Glide.with(this@MainActivity)
                        .load(it.albumArtUri)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivFullArt)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isPlaying.collect { isPlaying ->
                val miniIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                val fullIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                btnMiniPlayPause.setImageResource(miniIcon)
                btnPlayPause.setImageResource(fullIcon)
                toggleSeekbarUpdate(isPlaying)
            }
        }

        lifecycleScope.launch {
            viewModel.isShuffleModeEnabled.collect { isShuffleEnabled ->
                val color = if (isShuffleEnabled) ContextCompat.getColor(this@MainActivity, R.color.accent)
                else ContextCompat.getColor(this@MainActivity, R.color.gray)
                btnShuffle.setColorFilter(color)
            }
        }
    }

    private fun toggleSeekbarUpdate(isPlaying: Boolean) {
        updateSeekbarJob?.cancel()
        if (isPlaying) {
            updateSeekbarJob = lifecycleScope.launch {
                while (isActive) {
                    seekBar.progress = viewModel.getCurrentPosition().toInt()
                    delay(1000L)
                }
            }
        }
    }

    private fun checkPermissionsAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadSongs()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    override fun onDestroy() {
        updateSeekbarJob?.cancel()
        super.onDestroy()
    }
}
