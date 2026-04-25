package com.ade.habittracker.data

import androidx.compose.ui.graphics.Color
import androidx.annotation.RawRes
import com.ade.habittracker.R

// --- ITEM TOKO ---
sealed class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val type: ShopType
)

enum class ShopType { THEME, AVATAR, CHIBI, SPLASH, AVATAR_FRAME, MISSION_CARD, CHECKLIST_EFFECT, BUNDLE }

// 1. TEMA (Warna Aplikasi)
data class ThemeItem(
    val themeId: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val cost: Int,
    val themeDescription: String = "Ubah warna antarmuka."
) : ShopItem(themeId, displayName, themeDescription, cost, ShopType.THEME)

// 2. AVATAR (Icon User)
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

data class AvatarFrameItem(
    val frameId: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val cost: Int,
    val frameDescription: String
) : ShopItem(frameId, displayName, frameDescription, cost, ShopType.AVATAR_FRAME)

data class MissionCardSkinItem(
    val skinId: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val cost: Int,
    val skinDescription: String
) : ShopItem(skinId, displayName, skinDescription, cost, ShopType.MISSION_CARD)

data class ChecklistEffectItem(
    val effectId: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val emoji: String,
    val cost: Int,
    val effectDescription: String
) : ShopItem(effectId, displayName, effectDescription, cost, ShopType.CHECKLIST_EFFECT)

data class BundleItem(
    val bundleId: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val includedItemIds: List<String>,
    val cost: Int,
    val bundleDescription: String
) : ShopItem(bundleId, displayName, bundleDescription, cost, ShopType.BUNDLE)

// --- DATABASE ITEM ---
object ShopRepository {

    val THEMES = listOf(
        ThemeItem("theme_default", "Original Dark", Color(0xFF6366f1), Color(0xFF1e293b), 0),
        ThemeItem("theme_ramadhan", "Ramadhan Calm", Color(0xFF2FA16E), Color(0xFF071C14), 0),
        ThemeItem(
            "theme_ikuyo",
            "Ikuyo Scarlet",
            Color(0xFFFF5A6D),
            Color(0xFF24060A),
            0,
            "Nuansa panggung merah hangat dengan wallpaper Ikuyo yang bikin BitQuest terasa lebih cerah, berani, dan hidup."
        ),
        ThemeItem(
            "theme_hutao",
            "Hu Tao Ember",
            Color(0xFFFF7043),
            Color(0xFF1B0606),
            0,
            "Nuansa merah bara dengan wallpaper Hu Tao yang bikin BitQuest terasa misterius, playful, dan elegan."
        ),
        ThemeItem(
            "theme_furina",
            "Furina Tide",
            Color(0xFF6AC7FF),
            Color(0xFF07111F),
            0,
            "Nuansa biru panggung air dengan wallpaper Furina yang bikin BitQuest terasa mewah, teatrikal, dan elegan."
        ),
        ThemeItem(
            "theme_moonlit",
            "Moonlit Quest",
            Color(0xFF7FC7FF),
            Color(0xFF07101E),
            0,
            "Langit malam tenang dengan cahaya bulan lembut. Cocok buat sesi fokus, statistik, dan suasana yang elegan."
        ),
        ThemeItem(
            "theme_forest_camp",
            "Forest Camp",
            Color(0xFF7FE4A5),
            Color(0xFF09150F),
            0,
            "Basecamp petualang dengan nuansa hutan malam dan api unggun hangat. Rasanya santai, akrab, dan hidup."
        ),
        ThemeItem(
            "theme_arcade",
            "Starlight Arcade",
            Color(0xFF8A7DFF),
            Color(0xFF080714),
            0,
            "Arcade neon bergaya pixel dengan bintang dan glow futuristik. Lebih playful, lebih game-like, tetap premium."
        ),
        ThemeItem("theme_ramadhan_festive", "Ramadhan Festive", Color(0xFF4CC48A), Color(0xFF06150F), 1200),
        ThemeItem("theme_ocean", "Ocean Blue", Color(0xFF0ea5e9), Color(0xFF0f172a), 500),
        ThemeItem("theme_forest", "Toxic Green", Color(0xFF22c55e), Color(0xFF052e16), 800),
        ThemeItem("theme_sunset", "Sunset Orange", Color(0xFFf97316), Color(0xFF431407), 1000),
        ThemeItem("theme_royal", "Royal Gold", Color(0xFFeab308), Color(0xFF422006), 2000),
        ThemeItem("theme_blood", "Vampire Red", Color(0xFFef4444), Color(0xFF450a0a), 1500)
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

    val AVATAR_FRAMES = listOf(
        AvatarFrameItem(
            "frame_default",
            "Basic Ring",
            Color(0xFF7FC7FF),
            Color(0xFF3A3A3C),
            0,
            "Frame bersih bawaan untuk avatar profil."
        ),
        AvatarFrameItem(
            "frame_moonlight",
            "Moonlight Halo",
            Color(0xFF9DD6FF),
            Color(0xFF334B78),
            450,
            "Cincin bulan biru dengan glow tenang buat profil malam."
        ),
        AvatarFrameItem(
            "frame_forest",
            "Forest Leaf",
            Color(0xFF8EE7A8),
            Color(0xFF365F3F),
            520,
            "Frame hijau daun untuk vibe basecamp dan petualangan."
        ),
        AvatarFrameItem(
            "frame_arcade",
            "Arcade Neon",
            Color(0xFFAA9BFF),
            Color(0xFFFF5CE1),
            650,
            "Neon pixel terang untuk profil yang lebih game-like."
        ),
        AvatarFrameItem(
            "frame_legend",
            "Legend Crown",
            Color(0xFFFFD66B),
            Color(0xFFFF7A3D),
            1200,
            "Frame emas premium untuk petualang yang ingin tampil paling menonjol."
        )
    )

    val MISSION_CARD_SKINS = listOf(
        MissionCardSkinItem(
            "mission_card_default",
            "Default Quest",
            Color(0xFF00E5FF),
            Color(0xFF3A3A3C),
            0,
            "Tampilan kartu misi bawaan yang clean dan aman."
        ),
        MissionCardSkinItem(
            "mission_card_parchment",
            "Quest Parchment",
            Color(0xFFFFD47A),
            Color(0xFF5A3A16),
            500,
            "Kartu misi bergaya papan quest klasik dan hangat."
        ),
        MissionCardSkinItem(
            "mission_card_moonlit",
            "Moonlit Glass",
            Color(0xFF8BD3FF),
            Color(0xFF142540),
            650,
            "Glass card biru malam yang lebih tenang dan elegan."
        ),
        MissionCardSkinItem(
            "mission_card_forest",
            "Forest Note",
            Color(0xFF92F0A8),
            Color(0xFF122719),
            650,
            "Kartu hijau hutan yang adem untuk daftar misi harian."
        ),
        MissionCardSkinItem(
            "mission_card_arcade",
            "Neon Arcade",
            Color(0xFF9F8CFF),
            Color(0xFF17102F),
            800,
            "Kartu misi neon pixel yang lebih terang dan playful."
        )
    )

    val CHECKLIST_EFFECTS = listOf(
        ChecklistEffectItem(
            "checklist_effect_default",
            "Clean Pop",
            Color(0xFF0A84FF),
            Color(0xFF3A3A3C),
            "✓",
            0,
            "Efek centang bawaan yang simpel dan jelas."
        ),
        ChecklistEffectItem(
            "checklist_effect_sparkle",
            "Sparkle Clear",
            Color(0xFFFFD66B),
            Color(0xFFFF9E2C),
            "✦",
            350,
            "Selesai misi terasa seperti dapat kilau reward kecil."
        ),
        ChecklistEffectItem(
            "checklist_effect_pixel",
            "Pixel Burst",
            Color(0xFFAA9BFF),
            Color(0xFFFF5CE1),
            "▣",
            450,
            "Efek pixel neon untuk momen mission clear yang lebih game-like."
        ),
        ChecklistEffectItem(
            "checklist_effect_leaf",
            "Leaf Breeze",
            Color(0xFF8EE7A8),
            Color(0xFF2E7D45),
            "❧",
            450,
            "Sentuhan daun lembut untuk penyelesaian misi yang kalem."
        ),
        ChecklistEffectItem(
            "checklist_effect_flame",
            "Flame Clear",
            Color(0xFFFF7A3D),
            Color(0xFFFFD66B),
            "◆",
            600,
            "Efek bara hangat untuk misi yang terasa lebih powerful."
        )
    )

    val BUNDLES = listOf(
        BundleItem(
            "bundle_moonlit",
            "Moonlit Bundle",
            Color(0xFF9DD6FF),
            Color(0xFF142540),
            listOf("theme_moonlit", "frame_moonlight", "mission_card_moonlit", "checklist_effect_sparkle"),
            1200,
            "Paket malam elegan: tema, frame, kartu misi, dan efek checklist yang senada."
        ),
        BundleItem(
            "bundle_forest",
            "Forest Camp Bundle",
            Color(0xFF8EE7A8),
            Color(0xFF122719),
            listOf("theme_forest_camp", "frame_forest", "mission_card_forest", "checklist_effect_leaf"),
            1200,
            "Paket basecamp hangat untuk profil dan misi yang lebih natural."
        ),
        BundleItem(
            "bundle_arcade",
            "Starlight Arcade Bundle",
            Color(0xFFAA9BFF),
            Color(0xFFFF5CE1),
            listOf("theme_arcade", "frame_arcade", "mission_card_arcade", "checklist_effect_pixel"),
            1500,
            "Paket neon pixel yang bikin BitQuest terasa lebih game-like."
        )
    )

    fun getAllItems(): List<ShopItem> =
        THEMES + AVATARS + CHIBI_SKINS + SPLASH_VIDEOS + AVATAR_FRAMES + MISSION_CARD_SKINS + CHECKLIST_EFFECTS + BUNDLES

    fun getChibiResById(chibiId: String): Int {
        return CHIBI_SKINS.find { it.chibiId == chibiId }?.resId ?: R.drawable.chibi_helper
    }

    fun getSplashResById(splashId: String): Int {
        return SPLASH_VIDEOS.find { it.splashId == splashId }?.resId ?: R.raw.splash_video
    }

    fun getAvatarFrameById(frameId: String): AvatarFrameItem {
        return AVATAR_FRAMES.find { it.frameId == frameId } ?: AVATAR_FRAMES.first()
    }

    fun getMissionCardSkinById(skinId: String): MissionCardSkinItem {
        return MISSION_CARD_SKINS.find { it.skinId == skinId } ?: MISSION_CARD_SKINS.first()
    }

    fun getChecklistEffectById(effectId: String): ChecklistEffectItem {
        return CHECKLIST_EFFECTS.find { it.effectId == effectId } ?: CHECKLIST_EFFECTS.first()
    }
}

data class SoundtrackItem(
    val id: String,
    val name: String,
    @RawRes val resId: Int
)

object SoundtrackLibrary {
    val ITEMS = listOf(
        SoundtrackItem("soundtrack_1", "Morning Quest", R.raw.sountrack1),
        SoundtrackItem("soundtrack_2", "Pixel Rush", R.raw.sountrack2),
        SoundtrackItem("soundtrack_3", "Dreamy Checkpoint", R.raw.sountrack3),
        SoundtrackItem("soundtrack_4", "Mission Glow", R.raw.sountrack4),
        SoundtrackItem("soundtrack_5", "Victory Streak", R.raw.sountrack5),
        SoundtrackItem("soundtrack_6", "Night Grind", R.raw.sountrack6)
    )

    val DEFAULT_SELECTION: List<String> = ITEMS.map { it.id }
    const val DEFAULT_ID: String = "soundtrack_1"

    fun getById(id: String): SoundtrackItem? = ITEMS.find { it.id == id }
}
