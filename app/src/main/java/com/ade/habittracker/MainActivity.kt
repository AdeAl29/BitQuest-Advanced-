package com.ade.habittracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.ade.habittracker.data.ShopRepository
import com.ade.habittracker.ui.navigation.MainScreen
import com.ade.habittracker.ui.theme.HabitTrackerTheme
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    // ðŸ”Š MUSIK LATAR
    private var mediaPlayer: MediaPlayer? = null

    // Status Config
    private var isMusicAllowed = true
    private var currentMusicId = "music_default" // Melacak ID lagu yang sedang diputar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ðŸ”¥ 1. MENGAKTIFKAN MODE FULLSCREEN (EDGE-TO-EDGE) ðŸ”¥
        enableEdgeToEdge()
        viewModel.syncAccountWithFirebaseSession()

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
                        releaseMediaPlayer() // Matikan musik dulu
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        // Bersihkan stack agar user tidak bisa back ke MainActivity
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        return@collectLatest // Hentikan eksekusi blok ini
                    }

                    // --- B. LOGIKA MUSIK ---
                    val wasMusicAllowed = isMusicAllowed
                    isMusicAllowed = data.isMusicEnabled

                    // Cek 1: Apakah lagu diganti di Shop?
                    if (data.activeMusic != currentMusicId) {
                        currentMusicId = data.activeMusic
                        // Jika musik nyala, ganti lagu langsung
                        if (isMusicAllowed) {
                            releaseMediaPlayer()
                            startBackgroundMusic()
                        }
                    }
                    // Cek 2: Apakah tombol On/Off ditekan?
                    else if (isMusicAllowed != wasMusicAllowed) {
                        if (isMusicAllowed) {
                            if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                                startBackgroundMusic()
                            }
                        } else {
                            pauseBackgroundMusic()
                        }
                    }
                }
            }
        }
    }

    // --- LOGIKA LIFECYCLE & MUSIK ---

    override fun onStart() {
        super.onStart()

        // ðŸ“† Reset habit harian & Cek Season Bulanan
        viewModel.resetHabitsIfNewDay()
        viewModel.refreshReminderSchedulesIfEnabled()

        // ðŸ”Š Musik mulai (Hanya jika diizinkan di setting)
        if (isMusicAllowed) {
            startBackgroundMusic()
        }
    }

    override fun onStop() {
        super.onStop()
        // Pause musik saat aplikasi diminimize/keluar
        pauseBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Bersihkan resource musik saat aplikasi dimatikan total
        releaseMediaPlayer()
    }

    // --- FUNGSI AUDIO PLAYER DINAMIS ---

    private fun startBackgroundMusic() {
        // Guard Clause: Jika setting musik mati, jangan jalankan apapun
        if (!isMusicAllowed) return

        if (mediaPlayer == null) {
            // ðŸ”¥ CARI RESOURCE LAGU BERDASARKAN ID DARI SHOP ðŸ”¥
            val musicItem = ShopRepository.MUSICS.find { it.musicId == currentMusicId }
            val resId = musicItem?.resId ?: R.raw.sountrack // Fallback ke default jika error

            try {
                mediaPlayer = MediaPlayer.create(this, resId).apply {
                    isLooping = true // Musik berulang
                    setVolume(0.5f, 0.5f) // Volume 50%
                }
            } catch (e: Exception) {
                Log.e("MainActivityMusic", "Failed to create MediaPlayer: ${e.message}")
            }
        }

        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e("MainActivityMusic", "MediaPlayer start error: ${e.message}")
            releaseMediaPlayer()
            // Retry logic sederhana dengan lagu default aman
            try {
                mediaPlayer = MediaPlayer.create(this, R.raw.sountrack)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            } catch (e2: Exception) {
                Log.e("MainActivityMusic", "Retry failed")
            }
        }
    }

    private fun pauseBackgroundMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    // --- LOGIKA IZIN NOTIFIKASI (Android 13+) ---

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
}

