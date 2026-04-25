package com.ade.habittracker.ui.components.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.ui.viewmodel.AvatarItem

@Composable
fun AvatarPickerSheet(
    avatarList: List<Pair<AvatarItem, Boolean>>, // Terima List Pair (Avatar, isUnlocked)
    currentAvatarId: String,
    onAvatarSelected: (String) -> Unit,
    onPickFromGallery: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Button(
                onClick = onPickFromGallery,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentYellow,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .height(44.dp)
            ) {
                Text("Pilih Foto Dari Galeri", fontWeight = FontWeight.Bold)
            }
        }

        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Pilih Avatar",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(avatarList, key = { it.first.id }) { (avatar, isUnlocked) ->
            val isSelected = avatar.id == currentAvatarId

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    shape = CircleShape,
                    border = if (isSelected) BorderStroke(3.dp, AccentYellow) else null,
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier
                        .size(100.dp)
                        .clickable(enabled = isUnlocked) {
                            onAvatarSelected(avatar.id)
                        }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = avatar.resId),
                            contentDescription = "Avatar ${avatar.id}",
                            contentScale = ContentScale.Crop,
                            alpha = if (isUnlocked) 1.0f else 0.3f,
                            modifier = Modifier
                                .clip(CircleShape)
                                .padding(if (isSelected) 6.dp else 0.dp)
                        )

                        if (!isUnlocked) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Terkunci",
                                tint = TextColorPrimary.copy(alpha = 0.8f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lvl ${avatar.requiredLevel}",
                        color = TextColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
