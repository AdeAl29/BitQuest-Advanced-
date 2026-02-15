package com.ade.habittracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.ade.habittracker.data.ShopRepository

// --- Warna Default (Fallback) ---
val DefaultPrimary = Color(0xFF6366f1) // Indigo
val DefaultBackground = Color(0xFF020617) // Slate 950

@Composable
fun HabitTrackerTheme(
    activeThemeId: String = "theme_default", // 🔥 Parameter Dinamis dari AppData
    content: @Composable () -> Unit
) {
    // 1. Cari Tema dari Repository berdasarkan ID
    val themeItem = ShopRepository.THEMES.find { it.themeId == activeThemeId }

    // 2. Tentukan Warna (Pakai fallback jika ID tidak ditemukan/null)
    val primaryColor = themeItem?.primaryColor ?: DefaultPrimary
    val backgroundColor = themeItem?.secondaryColor ?: DefaultBackground

    // Warna sekunder tetap emas (Accent) agar konsisten dengan gamifikasi
    val secondaryColor = AccentYellow

    // 3. Konfigurasi Skema Warna (Dark Mode Oriented)
    val colorScheme = darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,

        secondary = secondaryColor,
        onSecondary = Color.Black,

        tertiary = primaryColor.copy(alpha = 0.7f),

        background = backgroundColor,
        onBackground = TextColorPrimary,

        surface = backgroundColor, // Surface mengikuti background agar immersive
        onSurface = TextColorPrimary,

        error = ErrorColor,
        onError = Color.White
    )

    // 4. Update System UI (Status Bar & Navigation Bar)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Ubah warna status bar & nav bar agar menyatu dengan tema
            window.statusBarColor = backgroundColor.toArgb()
            window.navigationBarColor = backgroundColor.toArgb()

            // Pastikan ikon status bar berwarna terang (putih) karena background gelap
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // 5. Terapkan Tema
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}