package com.ade.habittracker.ui.components.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.ui.viewmodel.TitleItem

@Composable
fun TitlePickerSheet(
    titleList: List<Pair<TitleItem, Boolean>>, // Pair<Gelar, isUnlocked>
    currentTitle: String,
    onTitleSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = "Pilih Gelar",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorPrimary,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn {
            items(titleList, key = { it.first.title }) { (titleItem, isUnlocked) ->
                val isSelected = titleItem.title == currentTitle

                TitleItemView(
                    title = titleItem.title,
                    requiredLevel = titleItem.requiredLevel,
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    onClick = {
                        if (isUnlocked) {
                            onTitleSelected(titleItem.title)
                        }
                    }
                )
                Divider(color = CardBackground, thickness = 1.dp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TitleItemView(
    title: String,
    requiredLevel: Int,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val titleColor = if (isUnlocked) TextColorPrimary else TextColorSecondary.copy(alpha = 0.5f)
    val iconColor = if (isSelected) AccentYellow else if (isUnlocked) PrimaryColor else TextColorSecondary.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 18.sp
            )
            if (!isUnlocked) {
                Text(
                    text = "Tersedia di Level $requiredLevel",
                    color = TextColorSecondary.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Dipilih",
                    tint = iconColor
                )
            } else if (!isUnlocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Terkunci",
                    tint = iconColor
                )
            }
        }
    }
}
