package com.ade.habittracker.data

// 🔥 Definisi Data Class HANYA DISINI
data class DailyReward(
    val day: Int,
    val coins: Int, // Menggunakan Coins (bukan XP/Ticket lagi)
    val desc: String
)

object DailyRewardsConfig {
    val REWARDS = listOf(
        DailyReward(1, 50, "Recehan nemu di jalan"),
        DailyReward(2, 100, "Sisa kembalian"),
        DailyReward(3, 150, "Tabungan celengan"),
        DailyReward(4, 200, "Gaji part-time"),
        DailyReward(5, 300, "Bonus lembur"),
        DailyReward(6, 500, "Menang lotre"),
        DailyReward(7, 1000, "JACKPOT SULTAN!")
    )
}