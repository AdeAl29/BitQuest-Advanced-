package com.ade.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.TextColorSecondary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProductivityHeatmap(
    heatmapData: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    // 🔥 UPDATE: Ubah jadi 5 minggu (agar mencakup 30 hari penuh)
    val weeksToShow = 5
    val daysPerWeek = 7

    // Siapkan Data Tanggal
    val calendarData = remember {
        val list = mutableListOf<Pair<String, Date>>()
        val cal = Calendar.getInstance()

        // Geser mundur agar menampilkan range yang pas
        cal.add(Calendar.DAY_OF_YEAR, -(weeksToShow * daysPerWeek) + 1)

        for (i in 0 until (weeksToShow * daysPerWeek)) {
            val date = cal.time
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            list.add(Pair(dateStr, date))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 🔥 UPDATE JUDUL HEADER
            Text(
                text = "KONSISTENSI (30 HARI)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid Heatmap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // Agar renggang rapi
            ) {
                for (week in 0 until weeksToShow) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (day in 0 until daysPerWeek) {
                            val index = (week * daysPerWeek) + day
                            if (index < calendarData.size) {
                                val (dateStr, _) = calendarData[index]
                                val count = heatmapData[dateStr] ?: 0

                                HeatmapBox(count = count)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", fontSize = 10.sp, color = TextColorSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                HeatmapBox(0, size = 10.dp)
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapBox(2, size = 10.dp)
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapBox(5, size = 10.dp)
                Spacer(modifier = Modifier.width(2.dp))
                HeatmapBox(8, size = 10.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("More", fontSize = 10.sp, color = TextColorSecondary)
            }
        }
    }
}

@Composable
fun HeatmapBox(
    count: Int,
    size: androidx.compose.ui.unit.Dp = 10.dp
) {
    val color = when {
        count == 0 -> Color.White.copy(alpha = 0.1f)
        count in 1..2 -> AccentYellow.copy(alpha = 0.3f)
        count in 3..5 -> AccentYellow.copy(alpha = 0.6f)
        else -> AccentYellow
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}