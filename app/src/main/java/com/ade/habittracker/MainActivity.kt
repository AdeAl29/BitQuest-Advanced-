package com.ade.habittracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ade.habittracker.data.SoundtrackLibrary
import com.ade.habittracker.ui.navigation.MainScreen
import com.ade.habittracker.ui.theme.HabitTrackerTheme
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    private var soundtrackPlayer: MediaPlayer? = null
    private var isOriginalSoundtrackEnabled = true
    private var currentSoundtrackId = SoundtrackLibrary.DEFAULT_ID
    private var selectedSoundtrackIds = SoundtrackLibrary.DEFAULT_SELECTION
    private var loadedSoundtrackId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ðŸ”¥ 1. MENGAKTIFKAN MODE FULLSCREEN (EDGE-TO-EDGE) ðŸ”¥
        enableEdgeToEdge()

        setContent {
            // Ambil data terbaru untuk Tema
            val appDataState by viewModel.appData.collectAsStateWithLifecycle()
            val activeThemeId = appDataState?.activeTheme ?: "theme_default"

            // ðŸ”¥ 2. TERAPKAN TEMA DINAMIS DARI SHOP ðŸ”¥
            HabitTrackerTheme(activeThemeId = activeThemeId) {

                // Surface menggunakan warna background dari Tema yang dipilih
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        onScheduleReminderClick = {
                            askNotificationPermission()
                        }
                    )
                }
            }
        }

        // ðŸ”¥ 3. OBSERVER PENGATURAN MUSIK, LAGU AKTIF, & STATUS LOGOUT ðŸ”¥
        lifecycleScope.launch {
            viewModel.appData.collectLatest { data ->
                if (data != null) {

                    // --- A. LOGIKA LOGOUT ---
                    // Jika user menekan tombol logout, isLoggedIn menjadi false.
                    // Kita harus kembali ke LoginActivity.
                    if (!data.isLoggedIn) {
                        runCatching {
                            if (FirebaseApp.getApps(this@MainActivity).isNotEmpty()) {
                                FirebaseAuth.getInstance().signOut()
                            }
                        }
                        stopOriginalSoundtrack()
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        return@collectLatest
                    }

                    val wasOriginalSoundtrackEnabled = isOriginalSoundtrackEnabled
                    isOriginalSoundtrackEnabled = data.isOriginalSoundtrackEnabled
                    val sanitizedSelection = data.selectedSoundtracks
                        .filter { id -> SoundtrackLibrary.getById(id) != null }
                        .ifEmpty { SoundtrackLibrary.DEFAULT_SELECTION }
                    val playlistChanged = sanitizedSelection != selectedSoundtrackIds
                    val soundtrackChanged = data.currentSoundtrack != currentSoundtrackId

                    selectedSoundtrackIds = sanitizedSelection
                    currentSoundtrackId = data.currentSoundtrack
                        .takeIf { id -> id in selectedSoundtrackIds && SoundtrackLibrary.getById(id) != null }
                        ?: selectedSoundtrackIds.first()

                    if (!isOriginalSoundtrackEnabled) {
                        stopOriginalSoundtrack()
                    } else if (
                        lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED) &&
                        (
                            soundtrackPlayer == null ||
                                wasOriginalSoundtrackEnabled != isOriginalSoundtrackEnabled ||
                                playlistChanged ||
                                soundtrackChanged
                            )
                    ) {
                        startOriginalSoundtrack(forceRestart = playlistChanged || soundtrackChanged)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.resetHabitsIfNewDay()
        viewModel.refreshReminderSchedulesIfEnabled()

        if (isOriginalSoundtrackEnabled) {
            startOriginalSoundtrack(forceRestart = soundtrackPlayer == null)
        }
    }

    override fun onStop() {
        super.onStop()
        stopOriginalSoundtrack()
    }

    override fun onDestroy() {
        stopOriginalSoundtrack()
        super.onDestroy()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.scheduleDailyReminder(applicationContext)
                Toast.makeText(this, "Pengingat harian diaktifkan!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                viewModel.scheduleDailyReminder(applicationContext)
                Toast.makeText(this, "Pengingat harian disinkronkan.", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Untuk Android 12 ke bawah, izin otomatis diberikan saat install
            viewModel.scheduleDailyReminder(applicationContext)
            Toast.makeText(this, "Pengingat harian diaktifkan!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startOriginalSoundtrack(forceRestart: Boolean = false) {
        if (!isOriginalSoundtrackEnabled) return

        val targetTrackId = currentSoundtrackId
            .takeIf { it in selectedSoundtrackIds && SoundtrackLibrary.getById(it) != null }
            ?: selectedSoundtrackIds.firstOrNull()
            ?: SoundtrackLibrary.DEFAULT_ID

        val shouldRecreate = forceRestart || soundtrackPlayer == null || loadedSoundtrackId != targetTrackId
        if (shouldRecreate) {
            stopOriginalSoundtrack()
            loadedSoundtrackId = targetTrackId
            val resId = SoundtrackLibrary.getById(targetTrackId)?.resId ?: R.raw.sountrack1
            soundtrackPlayer = MediaPlayer.create(this, resId)?.apply {
                isLooping = false
                setVolume(0.45f, 0.45f)
                setOnCompletionListener {
                    playNextSoundtrack()
                }
            }
        }

        if (soundtrackPlayer?.isPlaying != true) {
            soundtrackPlayer?.start()
        }
    }

    private fun playNextSoundtrack() {
        val safePlaylist = selectedSoundtrackIds
            .filter { SoundtrackLibrary.getById(it) != null }
            .ifEmpty { SoundtrackLibrary.DEFAULT_SELECTION }
        if (safePlaylist.isEmpty()) {
            stopOriginalSoundtrack()
            return
        }

        val currentIndex = safePlaylist.indexOf(currentSoundtrackId).takeIf { it >= 0 } ?: -1
        val nextIndex = (currentIndex + 1).mod(safePlaylist.size)
        val nextTrackId = safePlaylist[nextIndex]
        currentSoundtrackId = nextTrackId
        viewModel.setCurrentSoundtrack(nextTrackId)
        startOriginalSoundtrack(forceRestart = true)
    }

    private fun stopOriginalSoundtrack() {
        try {
            soundtrackPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            soundtrackPlayer?.release()
        } catch (_: Exception) {
        } finally {
            soundtrackPlayer = null
            loadedSoundtrackId = null
        }
    }
}

