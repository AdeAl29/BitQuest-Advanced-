package com.ade.habittracker.ui.components.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ade.habittracker.data.PersonalizedRecommendedMission
import com.ade.habittracker.data.findPersonalizedRecommendedMissions
import com.ade.habittracker.data.recommendationActivityOptions
import com.ade.habittracker.data.recommendationDifficultyHint
import com.ade.habittracker.data.recommendationFieldsForActivity
import com.ade.habittracker.data.recommendationStageLabel
import com.ade.habittracker.data.recommendationStagesForActivity
import com.ade.habittracker.model.HabitTemplate
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.PrimaryColor
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary

@Composable
fun RecommendationHabitSheet(
    userLevel: Int,
    userStreak: Int,
    initialActivityType: String,
    initialField: String,
    initialStage: String,
    initialAge: Int?,
    onPreferencesChanged: (activityType: String, field: String, stage: String, age: Int?) -> Unit,
    onRecommendationClick: (HabitTemplate) -> Unit
) {
    val resolvedInitialActivity = initialActivityType.takeIf { it in recommendationActivityOptions }
        ?: recommendationActivityOptions.first()
    var selectedActivity by remember { mutableStateOf(resolvedInitialActivity) }
    val fieldOptions = remember(selectedActivity) { recommendationFieldsForActivity(selectedActivity) }
    val stageOptions = remember(selectedActivity) { recommendationStagesForActivity(selectedActivity) }
    val stageLabel = remember(selectedActivity) { recommendationStageLabel(selectedActivity) }

    var selectedField by remember {
        mutableStateOf(initialField.takeIf { it in fieldOptions } ?: fieldOptions.firstOrNull().orEmpty())
    }
    var selectedStage by remember {
        mutableStateOf(initialStage.takeIf { it in stageOptions } ?: stageOptions.firstOrNull().orEmpty())
    }
    var ageText by remember { mutableStateOf(initialAge?.toString().orEmpty()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(fieldOptions) {
        if (selectedField !in fieldOptions) {
            selectedField = fieldOptions.firstOrNull().orEmpty()
        }
    }

    LaunchedEffect(stageOptions) {
        if (selectedStage !in stageOptions) {
            selectedStage = stageOptions.firstOrNull().orEmpty()
        }
    }

    LaunchedEffect(selectedActivity, selectedField, selectedStage, ageText) {
        onPreferencesChanged(
            selectedActivity,
            selectedField,
            selectedStage,
            ageText.toIntOrNull()
        )
    }

    val recommendations = remember(
        selectedActivity,
        selectedField,
        selectedStage,
        ageText,
        searchQuery,
        userLevel,
        userStreak
    ) {
        if (selectedField.isBlank() || selectedStage.isBlank()) {
            emptyList()
        } else {
            findPersonalizedRecommendedMissions(
                activityType = selectedActivity,
                field = selectedField,
                stage = selectedStage,
                age = ageText.toIntOrNull(),
                userLevel = userLevel,
                userStreak = userStreak,
                query = searchQuery
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Rekomendasi Misi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColorPrimary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            Text(
                text = "Masukkan konteks aktivitas, umur, dan tahapmu sekarang. Sistem akan mengurutkan misi berdasarkan level serta streak akunmu.",
                color = TextColorSecondary,
                fontSize = 12.sp
            )
        }

        item {
            Text("Aktivitas", color = TextColorSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                recommendationActivityOptions.forEach { activity ->
                    FilterChip(
                        selected = selectedActivity == activity,
                        onClick = { selectedActivity = activity },
                        label = { Text(activity) }
                    )
                }
            }
        }

        item {
            Text("Jurusan atau Bidang", color = TextColorSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                fieldOptions.forEach { field ->
                    FilterChip(
                        selected = selectedField == field,
                        onClick = { selectedField = field },
                        label = { Text(field) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = ageText,
                onValueChange = { value -> ageText = value.filter(Char::isDigit).take(2) },
                label = { Text("Umur") },
                placeholder = { Text("Contoh: 16, 19, 24") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = recommendationTextFieldColors()
            )
        }

        item {
            Text(stageLabel, color = TextColorSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                stageOptions.forEach { stage ->
                    FilterChip(
                        selected = selectedStage == stage,
                        onClick = { selectedStage = stage },
                        label = { Text(stage) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari rekomendasi") },
                placeholder = { Text("Contoh: fokus, deadline, proyek") },
                modifier = Modifier.fillMaxWidth(),
                colors = recommendationTextFieldColors()
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.22f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${recommendations.size} rekomendasi siap dipakai untuk $selectedActivity • $selectedField • $selectedStage.",
                        color = TextColorPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Level $userLevel • streak $userStreak hari. ${recommendationDifficultyHint(userLevel, userStreak)}",
                        color = TextColorSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        items(recommendations, key = { it.mission.id }) { recommendation ->
            RecommendationMissionItem(
                recommendation = recommendation,
                onAddClick = { onRecommendationClick(recommendation.toHabitTemplate()) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun RecommendationMissionItem(
    recommendation: PersonalizedRecommendedMission,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = recommendation.mission.title,
                color = TextColorPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = recommendation.personalizedReason,
                color = TextColorSecondary,
                fontSize = 12.sp
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RecommendationTag(text = recommendation.fitLabel, accent = AccentYellow)
                RecommendationTag(text = recommendation.mission.activityType)
                RecommendationTag(text = recommendation.mission.field)
                RecommendationTag(text = recommendation.mission.schedule)
                RecommendationTag(text = "+${recommendation.mission.weight} XP", accent = PrimaryColor)
            }
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Tambah Misi Ini")
            }
        }
    }
}

@Composable
private fun RecommendationTag(
    text: String,
    accent: Color = PrimaryColor
) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
    ) {
        Text(
            text = text,
            color = accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun recommendationTextFieldColors() = androidx.compose.material3.TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedIndicatorColor = PrimaryColor,
    unfocusedIndicatorColor = TextColorSecondary,
    cursorColor = PrimaryColor,
    focusedTextColor = TextColorPrimary,
    unfocusedTextColor = TextColorPrimary,
    focusedLabelColor = PrimaryColor,
    unfocusedLabelColor = TextColorSecondary
)
