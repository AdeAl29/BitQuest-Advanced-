package com.ade.habittracker.data

import androidx.compose.ui.graphics.Color
import com.ade.habittracker.R

// --- ITEM TOKO ---
sealed class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val type: ShopType
)

enum class ShopType { THEME, MUSIC, AVATAR, CHIBI, SPLASH }

// 1. TEMA (Warna Aplikasi)
data class ThemeItem(
    val themeId: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val cost: Int
) : ShopItem(themeId, displayName, "Ubah warna antarmuka.", cost, ShopType.THEME)

// 2. MUSIK (Latar Belakang)
data class MusicItem(
    val musicId: String,
    val displayName: String,
    val resId: Int, // Resource ID dari R.raw
    val cost: Int
) : ShopItem(musicId, displayName, "Ubah suasana musik.", cost, ShopType.MUSIC)

// 3. AVATAR (Icon User)
data class AvatarShopItem(
    val avatarId: String,
    val displayName: String,
    val resId: Int,
    val cost: Int
) : ShopItem(avatarId, displayName, "Tampil beda di leaderboard.", cost, ShopType.AVATAR)

// 4. CHIBI SKIN (Asisten)
data class ChibiSkinItem(
    val chibiId: String,
    val displayName: String,
    val resId: Int,
    val cost: Int
) : ShopItem(chibiId, displayName, "Ganti skin asisten chibi.", cost, ShopType.CHIBI)

// 5. SPLASH VIDEO
data class SplashVideoItem(
    val splashId: String,
    val displayName: String,
    val resId: Int,
    val cost: Int
) : ShopItem(splashId, displayName, "Ganti video pembuka aplikasi.", cost, ShopType.SPLASH)

// --- DATABASE ITEM ---
object ShopRepository {

    val THEMES = listOf(
        ThemeItem("theme_default", "Original Dark", Color(0xFF6366f1), Color(0xFF1e293b), 0),
        ThemeItem("theme_ramadhan", "Ramadhan Calm", Color(0xFF2FA16E), Color(0xFF071C14), 0),
        ThemeItem("theme_ramadhan_festive", "Ramadhan Festive", Color(0xFF4CC48A), Color(0xFF06150F), 1200),
        ThemeItem("theme_ocean", "Ocean Blue", Color(0xFF0ea5e9), Color(0xFF0f172a), 500),
        ThemeItem("theme_forest", "Toxic Green", Color(0xFF22c55e), Color(0xFF052e16), 800),
        ThemeItem("theme_sunset", "Sunset Orange", Color(0xFFf97316), Color(0xFF431407), 1000),
        ThemeItem("theme_royal", "Royal Gold", Color(0xFFeab308), Color(0xFF422006), 2000),
        ThemeItem("theme_blood", "Vampire Red", Color(0xFFef4444), Color(0xFF450a0a), 1500)
    )

    val MUSICS = listOf(
        // Pastikan file mp3 ada di res/raw, kalau tidak ada ganti ke R.raw.sountrack (default)
        MusicItem("music_default", "Chill Lo-Fi", R.raw.sountrack, 0),
        MusicItem("music_piano", "Sad Piano", R.raw.sountrack, 500),
        MusicItem("music_beat", "Upbeat Work", R.raw.sountrack, 800)
    )

    val AVATARS = listOf(
        AvatarShopItem("avatar_level1", "Novice", R.drawable.avatar_level1, 0),
        AvatarShopItem("avatar_ninja", "Ninja", R.drawable.avatar_level5, 300),
        AvatarShopItem("avatar_king", "King", R.drawable.avatar_master, 1000)
    )

    val CHIBI_SKINS = listOf(
        ChibiSkinItem("chibi_helper", "Default Helper", R.drawable.chibi_helper, 0),
        ChibiSkinItem("chibi_ninja", "Chibi Ninja", R.drawable.chibi_ninja, 650),
        ChibiSkinItem("chibi_school", "Chibi School", R.drawable.chibi_school, 700),
        ChibiSkinItem("chibi_ramadhan", "Chibi Ramadhan", R.drawable.chibi_ramadhan, 750)
    )

    val SPLASH_VIDEOS = listOf(
        SplashVideoItem("splash_default", "Splash Default", R.raw.splash_video, 0),
        SplashVideoItem("splash_ramadhan", "Splash Ramadhan", R.raw.splash_ramadhan, 0)
    )

    fun getAllItems(): List<ShopItem> = THEMES + MUSICS + AVATARS + CHIBI_SKINS + SPLASH_VIDEOS

    fun getChibiResById(chibiId: String): Int {
        return CHIBI_SKINS.find { it.chibiId == chibiId }?.resId ?: R.drawable.chibi_helper
    }

    fun getSplashResById(splashId: String): Int {
        return SPLASH_VIDEOS.find { it.splashId == splashId }?.resId ?: R.raw.splash_video
    }
}
