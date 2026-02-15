package com.ade.habittracker.model

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,

    // 🔥 Variable Gambar (Pastikan namanya imageResId, BUKAN iconResId)
    @DrawableRes val imageResId: Int,

    val isUnlocked: Boolean = false,
    val progress: Int = 0,

    // 🔥 Variable Target (Pastikan namanya target, BUKAN goal)
    val target: Int = 1
)