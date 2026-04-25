package com.ade.habittracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.R
import com.ade.habittracker.data.AvatarFrameItem
import com.ade.habittracker.data.BundleItem
import com.ade.habittracker.data.ChecklistEffectItem
import com.ade.habittracker.data.ChibiSkinItem
import com.ade.habittracker.data.MissionCardSkinItem
import com.ade.habittracker.data.ShopItem
import com.ade.habittracker.data.ShopRepository
import com.ade.habittracker.data.ShopType
import com.ade.habittracker.data.SplashVideoItem
import com.ade.habittracker.data.ThemeItem
import com.ade.habittracker.model.AppData
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

private enum class ShopOwnershipFilter(val label: String) {
    ALL("Semua"),
    OWNED("Dimiliki"),
    ACTIVE("Aktif"),
    LOCKED("Belum")
}

@Composable
fun ShopScreen(
    appData: AppData,
    onBuy: (ShopItem) -> Unit,
    onEquip: (ShopItem) -> Unit
) {
    var selectedType by remember { mutableStateOf(ShopType.THEME) }
    var ownershipFilter by remember { mutableStateOf(ShopOwnershipFilter.ALL) }
    val themeAccent = MaterialTheme.colorScheme.primary
    val items = remember(selectedType, appData, ownershipFilter) {
        ShopRepository.getAllItems()
            .filter { it.type == selectedType }
            .filter { item ->
                val owned = isShopItemOwned(appData, item)
                val active = isShopItemActive(appData, item)
                when (ownershipFilter) {
                    ShopOwnershipFilter.ALL -> true
                    ShopOwnershipFilter.OWNED -> owned
                    ShopOwnershipFilter.ACTIVE -> active
                    ShopOwnershipFilter.LOCKED -> !owned
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TOKO PETUALANG",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = AccentYellow,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pilih tema, asisten, dan tampilan pembuka yang cocok buat suasana aplikasimu.",
            fontSize = 12.sp,
            color = TextColorSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.98f)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, themeAccent.copy(alpha = 0.22f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                themeAccent.copy(alpha = 0.14f),
                                CardBackground.copy(alpha = 0.98f),
                                CardBackground
                            )
                        )
                    )
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DOMPET ANDA",
                        fontSize = 11.sp,
                        color = themeAccent,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${appData.coins} Coins",
                        fontSize = 28.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (selectedType) {
                            ShopType.THEME -> "Ubah nuansa warna aplikasi"
                            ShopType.CHIBI -> "Pilih partner chibi favoritmu"
                            ShopType.SPLASH -> "Atur pembuka aplikasi"
                            ShopType.AVATAR_FRAME -> "Beri frame keren untuk avatar profil"
                            ShopType.MISSION_CARD -> "Ubah gaya kartu misi harian"
                            ShopType.CHECKLIST_EFFECT -> "Pilih efek saat misi selesai"
                            ShopType.BUNDLE -> "Paket item senada"
                            else -> "Atur koleksi"
                        },
                        fontSize = 12.sp,
                        color = TextColorSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(themeAccent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Coin",
                        tint = themeAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShopFilterChip(
                selected = selectedType == ShopType.THEME,
                label = "Tema",
                icon = Icons.Default.Palette,
                accent = themeAccent,
                onClick = { selectedType = ShopType.THEME }
            )
            ShopFilterChip(
                selected = selectedType == ShopType.CHIBI,
                label = "Chibi",
                icon = Icons.Default.SmartToy,
                accent = themeAccent,
                onClick = { selectedType = ShopType.CHIBI }
            )
            ShopFilterChip(
                selected = selectedType == ShopType.SPLASH,
                label = "Splash",
                icon = Icons.Default.Movie,
                accent = themeAccent,
                onClick = { selectedType = ShopType.SPLASH }
            )
            ShopFilterChip(
                selected = selectedType == ShopType.AVATAR_FRAME,
                label = "Frame",
                icon = Icons.Default.Star,
                accent = themeAccent,
                onClick = { selectedType = ShopType.AVATAR_FRAME }
            )
            ShopFilterChip(
                selected = selectedType == ShopType.MISSION_CARD,
                label = "Kartu",
                icon = Icons.Default.Palette,
                accent = themeAccent,
                onClick = { selectedType = ShopType.MISSION_CARD }
            )
            ShopFilterChip(
                selected = selectedType == ShopType.CHECKLIST_EFFECT,
                label = "Efek",
                icon = Icons.Default.Check,
                accent = themeAccent,
                onClick = { selectedType = ShopType.CHECKLIST_EFFECT }
            )
            ShopFilterChip(
                selected = selectedType == ShopType.BUNDLE,
                label = "Bundle",
                icon = Icons.Default.Star,
                accent = themeAccent,
                onClick = { selectedType = ShopType.BUNDLE }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShopOwnershipFilter.values().forEach { filter ->
                val selected = ownershipFilter == filter
                Text(
                    text = filter.label,
                    color = if (selected) Color.White else TextColorSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (selected) themeAccent.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f))
                        .clickable { ownershipFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = when (selectedType) {
                ShopType.THEME -> "Tema Aplikasi"
                ShopType.CHIBI -> "Skin Asisten Chibi"
                ShopType.SPLASH -> "Video Splash Pembuka"
                ShopType.AVATAR_FRAME -> "Frame Avatar"
                ShopType.MISSION_CARD -> "Skin Kartu Misi"
                ShopType.CHECKLIST_EFFECT -> "Efek Checklist"
                ShopType.BUNDLE -> "Bundle Tema"
                else -> "Item Shop"
            },
            color = themeAccent,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (selectedType) {
                ShopType.THEME -> if (appData.activeTheme == "theme_ikuyo") {
                    "Ikuyo Scarlet lagi aktif. Nuansa merah hangatnya bikin BitQuest terasa lebih panggung, berani, dan hidup."
                } else if (appData.activeTheme == "theme_hutao") {
                    "Hu Tao Ember lagi aktif. Nuansa merah bara dan gelapnya bikin BitQuest terasa misterius, playful, dan elegan."
                } else if (appData.activeTheme == "theme_furina") {
                    "Furina Tide lagi aktif. Nuansa biru panggung airnya bikin BitQuest terasa mewah, dingin, dan teatrikal."
                } else if (appData.activeTheme == "theme_moonlit") {
                    "Moonlit Quest lagi aktif. Langit malamnya bikin BitQuest terasa tenang, elegan, dan fokus."
                } else if (appData.activeTheme == "theme_forest_camp") {
                    "Forest Camp lagi aktif. Nuansa basecamp hangatnya bikin BitQuest terasa santai, akrab, dan nyaman."
                } else if (appData.activeTheme == "theme_arcade") {
                    "Starlight Arcade lagi aktif. Neon pixel-nya bikin BitQuest terasa seru, terang, dan game-like."
                } else {
                    "Bikin suasana aplikasi lebih hidup dengan warna dan mood berbeda."
                }
                ShopType.CHIBI -> "Set karakter pendamping yang paling cocok buat kamu."
                ShopType.SPLASH -> "Pilih tampilan pembuka yang terasa paling pas."
                ShopType.AVATAR_FRAME -> "Bikin avatar profil lebih menonjol dengan ring, glow, dan aura khusus."
                ShopType.MISSION_CARD -> "Ganti nuansa daftar misi supaya aktivitas harian terasa lebih fresh."
                ShopType.CHECKLIST_EFFECT -> "Pilih gaya efek kecil saat misi selesai supaya progres terasa lebih puas."
                ShopType.BUNDLE -> "Paket visual lengkap yang langsung membuka beberapa item senada sekaligus."
                else -> "Koleksi item aktif"
            },
            color = TextColorSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val isOwned = when (item.type) {
                    ShopType.THEME -> appData.ownedThemes.contains(item.id) || item.price == 0
                    ShopType.CHIBI -> appData.ownedChibiSkins.contains(item.id)
                    ShopType.SPLASH -> appData.ownedSplashVideos.contains(item.id) || item.price == 0
                    ShopType.AVATAR_FRAME -> appData.ownedAvatarFrames.contains(item.id) || item.price == 0
                    ShopType.MISSION_CARD -> appData.ownedMissionCardSkins.contains(item.id) || item.price == 0
                    ShopType.CHECKLIST_EFFECT -> appData.ownedChecklistEffects.contains(item.id) || item.price == 0
                    ShopType.BUNDLE -> {
                        val bundle = item as? BundleItem
                        bundle?.includedItemIds?.all { includedId ->
                            includedId in appData.ownedThemes ||
                                includedId in appData.ownedAvatarFrames ||
                                includedId in appData.ownedMissionCardSkins ||
                                includedId in appData.ownedChecklistEffects
                        } ?: false
                    }
                    else -> true
                }
                val isActive = when (item.type) {
                    ShopType.THEME -> appData.activeTheme == item.id
                    ShopType.CHIBI -> appData.activeChibiSkin == item.id
                    ShopType.SPLASH -> appData.activeSplashVideo == item.id
                    ShopType.AVATAR_FRAME -> appData.activeAvatarFrame == item.id
                    ShopType.MISSION_CARD -> appData.activeMissionCardSkin == item.id
                    ShopType.CHECKLIST_EFFECT -> appData.activeChecklistEffect == item.id
                    ShopType.BUNDLE -> {
                        val bundle = item as? BundleItem
                        bundle?.includedItemIds?.all { includedId ->
                            appData.activeTheme == includedId ||
                                appData.activeAvatarFrame == includedId ||
                                appData.activeMissionCardSkin == includedId ||
                                appData.activeChecklistEffect == includedId
                        } ?: false
                    }
                    else -> false
                }

                ShopItemCard(
                    item = item,
                    isOwned = isOwned,
                    isActive = isActive,
                    canAfford = appData.coins >= item.price,
                    themeAccent = themeAccent,
                    onAction = {
                        if (isOwned) onEquip(item) else onBuy(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun ShopFilterChip(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accent.copy(alpha = 0.18f),
            selectedLabelColor = TextColorPrimary,
            selectedLeadingIconColor = accent
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) accent.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.1f),
            selectedBorderColor = accent.copy(alpha = 0.28f)
        )
    )
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    isActive: Boolean,
    canAfford: Boolean,
    themeAccent: Color,
    onAction: () -> Unit
) {
    val rarity = remember(item.id, item.price, item.type) { rarityForShopItem(item) }
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            when {
                isActive -> themeAccent.copy(alpha = 0.45f)
                isOwned -> AccentYellow.copy(alpha = 0.18f)
                else -> Color.White.copy(alpha = 0.08f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.04f),
                                Color.Black.copy(alpha = 0.28f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (item) {
                    is ThemeItem -> {
                        if (
                            item.themeId == "theme_ikuyo" ||
                            item.themeId == "theme_hutao" ||
                            item.themeId == "theme_furina" ||
                            item.themeId == "theme_moonlit" ||
                            item.themeId == "theme_forest_camp" ||
                            item.themeId == "theme_arcade"
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                            ) {
                                when (item.themeId) {
                                    "theme_ikuyo", "theme_hutao", "theme_furina" -> {
                                        Image(
                                            painter = painterResource(
                                                id = when (item.themeId) {
                                                    "theme_ikuyo" -> R.drawable.ikuyo_tema
                                                    "theme_hutao" -> R.drawable.hutao_tema
                                                    else -> R.drawable.furina_tema
                                                }
                                            ),
                                            contentDescription = item.displayName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    "theme_moonlit" -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFF10203E),
                                                            Color(0xFF0A1427),
                                                            Color(0xFF060B14)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                    "theme_forest_camp" -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFF12321D),
                                                            Color(0xFF0B1A11),
                                                            Color(0xFF050A07)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFF1B1240),
                                                            Color(0xFF120D28),
                                                            Color(0xFF090711)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(colors = previewOverlayForTheme(item.themeId))
                                        )
                                )
                                Text(
                                    text = previewLabelForTheme(item.themeId),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                )
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(item.primaryColor, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(item.secondaryColor, CircleShape)
                                )
                            }
                        }
                    }
                    is ChibiSkinItem -> {
                        Image(
                            painter = painterResource(id = item.resId),
                            contentDescription = item.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.15f))
                        )
                    }
                    is SplashVideoItem -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = AccentYellow,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (item.id == "splash_default") "Default" else "Special",
                                color = TextColorPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    is AvatarFrameItem -> {
                        AvatarFramePreview(item = item)
                    }
                    is MissionCardSkinItem -> {
                        MissionCardSkinPreview(item = item)
                    }
                    is ChecklistEffectItem -> {
                        ChecklistEffectPreview(item = item)
                    }
                    is BundleItem -> {
                        BundlePreview(item = item)
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                val badgeText = when {
                    isActive -> "Aktif"
                    isOwned -> "Dimiliki"
                    else -> "${item.price} Coin"
                }
                val badgeColor = when {
                    isActive -> themeAccent
                    isOwned -> AccentYellow
                    else -> Color.White.copy(alpha = 0.8f)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = item.name,
                fontWeight = FontWeight.ExtraBold,
                color = TextColorPrimary,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = rarity,
                fontSize = 10.sp,
                color = rarityColor(rarity),
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(rarityColor(rarity).copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = item.description,
                fontSize = 10.sp,
                color = TextColorSecondary,
                lineHeight = 13.sp,
                minLines = 2
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAction,
                enabled = isOwned || canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isActive -> Color.Gray
                        isOwned -> themeAccent
                        else -> AccentYellow
                    },
                    contentColor = when {
                        isActive -> Color.White
                        isOwned -> Color.White
                        else -> Color.Black
                    },
                    disabledContainerColor = Color(0xFF3A3A3A),
                    disabledContentColor = TextColorSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                when {
                    isActive -> {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Dipakai", fontSize = 12.sp)
                    }
                    isOwned -> {
                        Text("Pasang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    else -> {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.size(4.dp))
                        Text(text = "${item.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarFramePreview(item: AvatarFrameItem) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            item.primaryColor.copy(alpha = 0.5f),
                            item.secondaryColor.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(item.primaryColor, item.secondaryColor)
                    )
                )
                .border(4.dp, item.primaryColor.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun MissionCardSkinPreview(item: MissionCardSkinItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = item.secondaryColor.copy(alpha = 0.88f)),
        border = BorderStroke(1.dp, item.primaryColor.copy(alpha = 0.42f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(7.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(item.primaryColor, item.primaryColor.copy(alpha = 0.35f))
                        )
                    )
            )
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White.copy(alpha = 0.42f))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(item.primaryColor.copy(alpha = 0.22f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistEffectPreview(item: ChecklistEffectItem) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            item.primaryColor.copy(alpha = 0.34f),
                            item.secondaryColor.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(item.primaryColor.copy(alpha = 0.92f), item.secondaryColor)
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.emoji,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun BundlePreview(item: BundleItem) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        item.primaryColor.copy(alpha = 0.26f),
                        item.secondaryColor.copy(alpha = 0.86f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 1) 42.dp else 34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(item.primaryColor.copy(alpha = 0.9f), item.secondaryColor)
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (index) {
                            0 -> Icons.Default.Palette
                            1 -> Icons.Default.Star
                            2 -> Icons.Default.Check
                            else -> Icons.Default.Palette
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Text(
            text = "4 in 1",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        )
    }
}

private fun previewOverlayForTheme(themeId: String): List<Color> {
    return when (themeId) {
        "theme_ikuyo" -> listOf(
            Color(0x55FF667C),
            Color(0xAA2A0710),
            Color(0xD9130206)
        )
        "theme_hutao" -> listOf(
            Color(0x55FF8A65),
            Color(0xAA250909),
            Color(0xD9100303)
        )
        "theme_furina" -> listOf(
            Color(0x558CD7FF),
            Color(0xAA0A1B33),
            Color(0xD9050B16)
        )
        "theme_moonlit" -> listOf(
            Color(0x4495CFFF),
            Color(0xAA091224),
            Color(0xD9040812)
        )
        "theme_forest_camp" -> listOf(
            Color(0x44A7E78B),
            Color(0xAA122114),
            Color(0xD9060A07)
        )
        else -> listOf(
            Color(0x558D7DFF),
            Color(0xAA120C28),
            Color(0xD9060712)
        )
    }
}

private fun previewLabelForTheme(themeId: String): String {
    return when (themeId) {
        "theme_ikuyo" -> "Scarlet Live"
        "theme_hutao" -> "Ember Smile"
        "theme_furina" -> "Ocean Aria"
        "theme_moonlit" -> "Night Pulse"
        "theme_forest_camp" -> "Camp Glow"
        else -> "Pixel Parade"
    }
}

private fun isShopItemOwned(appData: AppData, item: ShopItem): Boolean {
    return when (item.type) {
        ShopType.THEME -> appData.ownedThemes.contains(item.id) || item.price == 0
        ShopType.CHIBI -> appData.ownedChibiSkins.contains(item.id)
        ShopType.SPLASH -> appData.ownedSplashVideos.contains(item.id) || item.price == 0
        ShopType.AVATAR_FRAME -> appData.ownedAvatarFrames.contains(item.id) || item.price == 0
        ShopType.MISSION_CARD -> appData.ownedMissionCardSkins.contains(item.id) || item.price == 0
        ShopType.CHECKLIST_EFFECT -> appData.ownedChecklistEffects.contains(item.id) || item.price == 0
        ShopType.BUNDLE -> {
            val bundle = item as? BundleItem ?: return false
            bundle.includedItemIds.all { includedId ->
                includedId in appData.ownedThemes ||
                    includedId in appData.ownedAvatarFrames ||
                    includedId in appData.ownedMissionCardSkins ||
                    includedId in appData.ownedChecklistEffects
            }
        }
        ShopType.AVATAR -> true
    }
}

private fun isShopItemActive(appData: AppData, item: ShopItem): Boolean {
    return when (item.type) {
        ShopType.THEME -> appData.activeTheme == item.id
        ShopType.CHIBI -> appData.activeChibiSkin == item.id
        ShopType.SPLASH -> appData.activeSplashVideo == item.id
        ShopType.AVATAR_FRAME -> appData.activeAvatarFrame == item.id
        ShopType.MISSION_CARD -> appData.activeMissionCardSkin == item.id
        ShopType.CHECKLIST_EFFECT -> appData.activeChecklistEffect == item.id
        ShopType.BUNDLE -> {
            val bundle = item as? BundleItem ?: return false
            bundle.includedItemIds.all { includedId ->
                appData.activeTheme == includedId ||
                    appData.activeAvatarFrame == includedId ||
                    appData.activeMissionCardSkin == includedId ||
                    appData.activeChecklistEffect == includedId
            }
        }
        ShopType.AVATAR -> false
    }
}

private fun rarityForShopItem(item: ShopItem): String {
    return when {
        item.type == ShopType.BUNDLE -> "LEGENDARY"
        item.price == 0 -> "COMMON"
        item.price >= 1200 -> "LEGENDARY"
        item.price >= 800 -> "EPIC"
        item.price >= 450 -> "RARE"
        else -> "COMMON"
    }
}

private fun rarityColor(rarity: String): Color {
    return when (rarity) {
        "LEGENDARY" -> Color(0xFFFFD66B)
        "EPIC" -> Color(0xFFAA9BFF)
        "RARE" -> Color(0xFF64B5F6)
        else -> Color.White.copy(alpha = 0.75f)
    }
}
