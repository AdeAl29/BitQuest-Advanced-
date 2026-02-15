package com.ade.habittracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.data.ChibiSkinItem
import com.ade.habittracker.data.ShopItem
import com.ade.habittracker.data.ShopRepository
import com.ade.habittracker.data.ShopType
import com.ade.habittracker.data.SplashVideoItem
import com.ade.habittracker.data.ThemeItem
import com.ade.habittracker.model.AppData
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@Composable
fun ShopScreen(
    appData: AppData,
    onBuy: (ShopItem) -> Unit,
    onEquip: (ShopItem) -> Unit
) {
    var selectedType by remember { mutableStateOf(ShopType.THEME) }
    val items = remember(selectedType) {
        ShopRepository.getAllItems().filter { it.type == selectedType }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AccentYellow.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "DOMPET ANDA",
                        fontSize = 10.sp,
                        color = AccentYellow,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${appData.coins} Coins",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Coin",
                    tint = AccentYellow,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = selectedType == ShopType.THEME,
                onClick = { selectedType = ShopType.THEME },
                label = { Text("Tema") },
                leadingIcon = {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor.copy(alpha = 0.2f),
                    selectedLabelColor = TextColorPrimary
                )
            )
            FilterChip(
                selected = selectedType == ShopType.CHIBI,
                onClick = { selectedType = ShopType.CHIBI },
                label = { Text("Chibi") },
                leadingIcon = {
                    Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor.copy(alpha = 0.2f),
                    selectedLabelColor = TextColorPrimary
                )
            )
            FilterChip(
                selected = selectedType == ShopType.SPLASH,
                onClick = { selectedType = ShopType.SPLASH },
                label = { Text("Splash") },
                leadingIcon = {
                    Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor.copy(alpha = 0.2f),
                    selectedLabelColor = TextColorPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = when (selectedType) {
                ShopType.THEME -> "Tema Aplikasi"
                ShopType.CHIBI -> "Skin Asisten Chibi"
                ShopType.SPLASH -> "Video Splash Pembuka"
                else -> "Item Shop"
            },
            color = PrimaryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val isOwned = when (item.type) {
                    ShopType.THEME -> appData.ownedThemes.contains(item.id) || item.price == 0
                    ShopType.CHIBI -> appData.ownedChibiSkins.contains(item.id)
                    ShopType.SPLASH -> appData.ownedSplashVideos.contains(item.id) || item.price == 0
                    else -> true
                }
                val isActive = when (item.type) {
                    ShopType.THEME -> appData.activeTheme == item.id
                    ShopType.CHIBI -> appData.activeChibiSkin == item.id
                    ShopType.SPLASH -> appData.activeSplashVideo == item.id
                    else -> false
                }

                ShopItemCard(
                    item = item,
                    isOwned = isOwned,
                    isActive = isActive,
                    canAfford = appData.coins >= item.price,
                    onAction = {
                        if (isOwned) onEquip(item) else onBuy(item)
                    }
                )
            }
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    isActive: Boolean,
    canAfford: Boolean,
    onAction: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = if (isActive) BorderStroke(2.dp, PrimaryColor) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                when (item) {
                    is ThemeItem -> {
                        Row {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(item.primaryColor, CircleShape)
                            )
                            Spacer(Modifier.size(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(item.secondaryColor, CircleShape)
                            )
                        }
                    }
                    is ChibiSkinItem -> {
                        Image(
                            painter = painterResource(id = item.resId),
                            contentDescription = item.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(70.dp)
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
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (item.id == "splash_default") "Default" else "Ramadhan",
                                color = TextColorPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                fontSize = 14.sp
            )
            Text(
                text = item.description,
                fontSize = 10.sp,
                color = TextColorSecondary,
                lineHeight = 12.sp,
                minLines = 2
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAction,
                enabled = isOwned || canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isActive -> Color.Gray
                        isOwned -> PrimaryColor
                        else -> AccentYellow
                    },
                    contentColor = when {
                        isActive -> Color.White
                        isOwned -> Color.White
                        else -> Color.Black
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                when {
                    isActive -> {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Dipakai", fontSize = 12.sp)
                    }
                    isOwned -> {
                        Text("Pasang", fontSize = 12.sp)
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
