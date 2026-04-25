package com.ade.habittracker.data

import kotlin.random.Random

enum class LuckyRewardType {
    COINS,
    XP,
    TICKETS
}

data class LuckySpinReward(
    val id: String,
    val title: String,
    val description: String,
    val amount: Int,
    val type: LuckyRewardType,
    val rarityLabel: String,
    val accentHex: Long,
    val weight: Int
)

data class LuckySpinResult(
    val reward: LuckySpinReward,
    val usedTicket: Boolean
)

object LuckySpinConfig {
    val REWARDS = listOf(
        LuckySpinReward("coins_60", "Coin Kecil", "Tambahan receh yang tetap berguna.", 60, LuckyRewardType.COINS, "Common", 0xFFFFC95CL, 22),
        LuckySpinReward("coins_120", "Dompet Hangat", "Lumayan buat nambah tabungan petualang.", 120, LuckyRewardType.COINS, "Common", 0xFFFFD54FL, 18),
        LuckySpinReward("coins_220", "Panen Receh", "Koinnya mulai terasa enak dilihat.", 220, LuckyRewardType.COINS, "Rare", 0xFFFFB74DL, 14),
        LuckySpinReward("coins_450", "Treasure Pop", "Satu spin yang bikin dompetmu tersenyum.", 450, LuckyRewardType.COINS, "Epic", 0xFFFF9800L, 9),
        LuckySpinReward("coins_900", "Jackpot Mini", "Hadiah emas buat hari yang manis.", 900, LuckyRewardType.COINS, "Legend", 0xFFFF7043L, 4),
        LuckySpinReward("xp_25", "XP Ringan", "Sedikit dorongan buat level berikutnya.", 25, LuckyRewardType.XP, "Common", 0xFF81C784L, 18),
        LuckySpinReward("xp_60", "XP Burst", "Progress levelmu naik lebih cepat.", 60, LuckyRewardType.XP, "Rare", 0xFF66BB6AL, 13),
        LuckySpinReward("xp_120", "XP Surge", "Hadiah yang pas buat nge-push level.", 120, LuckyRewardType.XP, "Epic", 0xFF43A047L, 7),
        LuckySpinReward("xp_220", "Level Rush", "Kalau ini keluar, progressmu terasa banget.", 220, LuckyRewardType.XP, "Legend", 0xFF2E7D32L, 3),
        LuckySpinReward("ticket_1", "Tiket Bonus", "Simpan buat satu putaran keberuntungan lagi.", 1, LuckyRewardType.TICKETS, "Rare", 0xFF64B5F6L, 8),
        LuckySpinReward("ticket_2", "Double Ticket", "Dua tiket lagi buat ngetes hoki berikutnya.", 2, LuckyRewardType.TICKETS, "Epic", 0xFF42A5F5L, 3),
    )

    fun spin(random: Random = Random.Default): LuckySpinReward {
        val totalWeight = REWARDS.sumOf { it.weight }
        var roll = random.nextInt(totalWeight)
        REWARDS.forEach { reward ->
            roll -= reward.weight
            if (roll < 0) {
                return reward
            }
        }
        return REWARDS.first()
    }
}
