package com.ade.habittracker.data

import com.ade.habittracker.model.HabitTemplate
import java.util.Locale
import kotlin.math.abs

data class RecommendedMission(
    val id: String,
    val activityType: String,
    val field: String,
    val title: String,
    val schedule: String,
    val weight: Int,
    val reason: String
) {
    fun toHabitTemplate(): HabitTemplate = HabitTemplate(title, schedule, weight)
}

data class PersonalizedRecommendedMission(
    val mission: RecommendedMission,
    val personalizedReason: String,
    val fitLabel: String,
    val fitScore: Int
) {
    fun toHabitTemplate(): HabitTemplate = mission.toHabitTemplate()
}

val recommendationActivityOptions = listOf("Sekolah", "Kuliah", "Kerja")

private val schoolFieldOptions = listOf(
    "IPA",
    "IPS",
    "Bahasa",
    "Informatika",
    "RPL",
    "TKJ",
    "Multimedia",
    "Akuntansi"
)

private val collegeFieldOptions = listOf(
    "Informatika",
    "Sistem Informasi",
    "Kedokteran",
    "Hukum",
    "Psikologi",
    "Manajemen",
    "Akuntansi",
    "Arsitektur",
    "DKV",
    "Ilmu Komunikasi",
    "Teknik Industri",
    "Teknik Sipil"
)

private val workFieldOptions = listOf(
    "Software Engineer",
    "UI UX Designer",
    "Marketing",
    "Sales",
    "HR",
    "Finance",
    "Operations",
    "Teacher",
    "Healthcare",
    "Entrepreneur"
)

private val schoolStageOptions = listOf(
    "Kelas 7",
    "Kelas 8",
    "Kelas 9",
    "Kelas 10",
    "Kelas 11",
    "Kelas 12"
)

private val collegeStageOptions = (1..14).map { "Semester $it" }

private val workStageOptions = listOf(
    "Magang",
    "Junior",
    "Mid",
    "Senior",
    "Lead"
)

val recommendationFieldOptions: Map<String, List<String>> = mapOf(
    "Sekolah" to schoolFieldOptions,
    "Kuliah" to collegeFieldOptions,
    "Kerja" to workFieldOptions
)

private val recommendationStageOptions: Map<String, List<String>> = mapOf(
    "Sekolah" to schoolStageOptions,
    "Kuliah" to collegeStageOptions,
    "Kerja" to workStageOptions
)

val recommendedMissionPool: List<RecommendedMission> = buildList {
    addAll(
        generalPack(
            activityType = "Sekolah",
            items = listOf(
                Triple("Cek agenda pelajaran besok", "Setiap Malam", 10),
                Triple("Rapikan tas dan alat tulis", "Setiap Malam", 10),
                Triple("Review materi hari ini 15 menit", "Setiap Hari", 20),
                Triple("Kerjakan PR prioritas lebih dulu", "Setiap Hari", 30),
                Triple("Baca ulang catatan inti", "Senin-Jumat", 20),
                Triple("Belajar fokus 2 sesi pomodoro", "Senin-Jumat", 30),
                Triple("Kurangi scroll saat jam belajar", "Setiap Hari", 20),
                Triple("Tidur tepat waktu untuk sekolah", "Setiap Malam", 20)
            )
        )
    )
    addAll(
        generalPack(
            activityType = "Kuliah",
            items = listOf(
                Triple("Cek deadline tugas dan kuis", "Setiap Hari", 10),
                Triple("Review catatan kuliah 20 menit", "Setiap Hari", 20),
                Triple("Buka LMS dan rapikan to do kampus", "Setiap Hari", 20),
                Triple("Kerjakan tugas utama tanpa distraksi", "Setiap Hari", 30),
                Triple("Baca jurnal atau modul 5 halaman", "Senin-Jumat", 30),
                Triple("Susun target belajar mingguan", "3x Seminggu", 30),
                Triple("Pomodoro dua sesi untuk tugas kampus", "Setiap Hari", 30),
                Triple("Backup file tugas kuliah", "3x Seminggu", 20)
            )
        )
    )
    addAll(
        generalPack(
            activityType = "Kerja",
            items = listOf(
                Triple("Susun tiga prioritas kerja hari ini", "Setiap Pagi", 10),
                Triple("Inbox zero 15 menit", "Setiap Hari", 20),
                Triple("Blok waktu deep work 45 menit", "Senin-Jumat", 30),
                Triple("Kirim update progres singkat", "Senin-Jumat", 20),
                Triple("Rapikan catatan meeting", "Setiap Hari", 20),
                Triple("Follow up satu tugas penting", "Setiap Hari", 30),
                Triple("Evaluasi hasil kerja sebelum pulang", "Senin-Jumat", 20),
                Triple("Bereskan meja dan file kerja", "Setiap Hari", 10)
            )
        )
    )

    schoolFieldOptions.forEach { field -> addAll(schoolFieldPack(field)) }
    collegeFieldOptions.forEach { field -> addAll(collegeFieldPack(field)) }
    workFieldOptions.forEach { field -> addAll(workFieldPack(field)) }
}

fun recommendationFieldsForActivity(activityType: String): List<String> =
    recommendationFieldOptions[activityType].orEmpty()

fun recommendationStagesForActivity(activityType: String): List<String> =
    recommendationStageOptions[activityType].orEmpty()

fun recommendationStageLabel(activityType: String): String = when (activityType) {
    "Sekolah" -> "Kelas"
    "Kuliah" -> "Semester"
    else -> "Level Karier"
}

fun findRecommendedMissions(
    activityType: String,
    field: String,
    query: String = ""
): List<RecommendedMission> {
    val normalizedQuery = query.trim().lowercase(Locale.getDefault())
    return recommendedMissionPool
        .filter { mission ->
            mission.activityType == activityType &&
                (mission.field == "Umum" || mission.field.equals(field, ignoreCase = true))
        }
        .filter { mission ->
            normalizedQuery.isBlank() ||
                mission.title.lowercase(Locale.getDefault()).contains(normalizedQuery) ||
                mission.reason.lowercase(Locale.getDefault()).contains(normalizedQuery)
        }
        .sortedWith(
            compareByDescending<RecommendedMission> { it.field.equals(field, ignoreCase = true) }
                .thenByDescending { it.weight }
        )
}

fun findPersonalizedRecommendedMissions(
    activityType: String,
    field: String,
    stage: String,
    age: Int?,
    userLevel: Int,
    userStreak: Int,
    query: String = ""
): List<PersonalizedRecommendedMission> {
    val preferredWeight = preferredWeightFor(userLevel = userLevel, userStreak = userStreak)
    return findRecommendedMissions(activityType, field, query)
        .map { mission ->
            val score = scoreMission(
                mission = mission,
                activityType = activityType,
                field = field,
                stage = stage,
                age = age,
                preferredWeight = preferredWeight,
                userLevel = userLevel,
                userStreak = userStreak
            )
            PersonalizedRecommendedMission(
                mission = mission,
                personalizedReason = buildPersonalizedReason(
                    mission = mission,
                    activityType = activityType,
                    field = field,
                    stage = stage,
                    age = age,
                    userLevel = userLevel,
                    userStreak = userStreak,
                    preferredWeight = preferredWeight
                ),
                fitLabel = fitLabelFor(score),
                fitScore = score
            )
        }
        .sortedWith(
            compareByDescending<PersonalizedRecommendedMission> { it.fitScore }
                .thenByDescending { it.mission.field.equals(field, ignoreCase = true) }
                .thenBy { abs(it.mission.weight - preferredWeight) }
                .thenBy { it.mission.title }
        )
}

fun recommendationDifficultyHint(userLevel: Int, userStreak: Int): String = when (preferredWeightFor(userLevel, userStreak)) {
    20 -> "Fokus ke misi ringan-sedang agar ritme cepat kebentuk."
    30 -> "Kamu cocok dengan misi menengah yang stabil dan konsisten."
    else -> "Kamu siap menerima misi yang lebih menantang dan bernilai besar."
}

private fun preferredWeightFor(userLevel: Int, userStreak: Int): Int = when {
    userLevel >= 15 || userStreak >= 14 -> 50
    userLevel >= 6 || userStreak >= 5 -> 30
    else -> 20
}

private fun scoreMission(
    mission: RecommendedMission,
    activityType: String,
    field: String,
    stage: String,
    age: Int?,
    preferredWeight: Int,
    userLevel: Int,
    userStreak: Int
): Int {
    var score = 0

    score += if (mission.field.equals(field, ignoreCase = true)) 42 else 18
    score += (30 - abs(mission.weight - preferredWeight)).coerceAtLeast(4)

    if (userStreak <= 2 && mission.weight <= 30) score += 8
    if (userStreak >= 10 && mission.weight >= 30) score += 8
    if (userLevel >= 10 && mission.weight >= 30) score += 6

    when (activityType) {
        "Sekolah" -> {
            val classNumber = stage.filter(Char::isDigit).toIntOrNull()
            if (classNumber != null) {
                if (classNumber <= 9 && mission.weight <= 30) score += 7
                if (classNumber >= 10 && (mission.title.contains("target", true) || mission.title.contains("pomodoro", true))) score += 7
            }
        }
        "Kuliah" -> {
            val semesterNumber = stage.filter(Char::isDigit).toIntOrNull()
            if (semesterNumber != null) {
                if (semesterNumber <= 2 && (mission.title.contains("review", true) || mission.title.contains("rangkuman", true))) score += 8
                if (semesterNumber >= 5 && (mission.title.contains("proyek", true) || mission.title.contains("riset", true) || mission.weight >= 30)) score += 8
            }
        }
        "Kerja" -> {
            when (stage) {
                "Magang", "Junior" -> if (mission.title.contains("rapikan", true) || mission.title.contains("follow up", true) || mission.weight <= 30) score += 8
                "Senior", "Lead" -> if (mission.title.contains("update", true) || mission.title.contains("deep work", true) || mission.weight >= 30) score += 8
                else -> if (mission.weight in 20..30) score += 6
            }
        }
    }

    if (age != null) {
        when {
            age <= 16 && mission.weight <= 30 -> score += 6
            age in 17..22 && mission.weight in 20..50 -> score += 6
            age >= 23 && mission.weight >= 30 -> score += 6
        }
    }

    return score
}

private fun fitLabelFor(score: Int): String = when {
    score >= 85 -> "Sangat Cocok"
    score >= 72 -> "Cocok"
    else -> "Bisa Dicoba"
}

private fun buildPersonalizedReason(
    mission: RecommendedMission,
    activityType: String,
    field: String,
    stage: String,
    age: Int?,
    userLevel: Int,
    userStreak: Int,
    preferredWeight: Int
): String {
    val difficultyNote = when (preferredWeight) {
        20 -> "Karena progresmu masih membangun ritme, sistem lebih memprioritaskan misi yang mudah dijaga konsisten."
        30 -> "Karena level dan streak-mu sudah stabil, misi menengah akan memberi dorongan yang pas tanpa terlalu berat."
        else -> "Karena progresmu sudah kuat, sistem mendorong misi yang lebih menantang agar XP dan kualitas fokus naik."
    }

    val stageNote = when (activityType) {
        "Sekolah" -> "Disesuaikan untuk $stage di jalur $field supaya belajar harian lebih rapi."
        "Kuliah" -> "Disesuaikan untuk $stage jurusan $field agar tugas, modul, dan progres akademik lebih terarah."
        else -> "Disesuaikan untuk peran $stage di bidang $field agar ritme kerja tetap tajam."
    }

    val ageNote = age?.let { "Usia $it ikut dipakai untuk menyesuaikan beban misi yang terasa realistis." }.orEmpty()
    val streakNote = if (userStreak >= 7) {
        "Streak $userStreak hari menunjukkan kamu siap menjaga tantangan yang lebih stabil."
    } else {
        "Streak $userStreak hari artinya misi ini dipilih agar tetap gampang dipertahankan."
    }

    return "${mission.reason} $stageNote $difficultyNote $ageNote $streakNote".trim()
}

private fun generalPack(
    activityType: String,
    items: List<Triple<String, String, Int>>
): List<RecommendedMission> = items.mapIndexed { index, (title, schedule, weight) ->
    RecommendedMission(
        id = buildMissionId(activityType, "Umum", index),
        activityType = activityType,
        field = "Umum",
        title = title,
        schedule = schedule,
        weight = weight,
        reason = "Misi umum untuk rutinitas $activityType agar progres harian tetap rapi dan stabil."
    )
}

private fun schoolFieldPack(field: String): List<RecommendedMission> = listOf(
    mission("Sekolah", field, 0, "Latihan inti $field 25 menit", "Setiap Hari", 30),
    mission("Sekolah", field, 1, "Rangkum pelajaran $field hari ini", "Setiap Hari", 30),
    mission("Sekolah", field, 2, "Rapikan catatan dan PR $field", "Senin-Jumat", 20),
    mission("Sekolah", field, 3, "Review target nilai untuk $field", "3x Seminggu", 50),
    mission("Sekolah", field, 4, "Kerjakan latihan tambahan $field", "Senin-Jumat", 30),
    mission("Sekolah", field, 5, "Siapkan pertanyaan penting untuk pelajaran $field", "3x Seminggu", 20),
    mission("Sekolah", field, 6, "Baca materi pengantar $field 15 menit", "Setiap Hari", 20),
    mission("Sekolah", field, 7, "Tes mandiri singkat untuk $field", "3x Seminggu", 30)
)

private fun collegeFieldPack(field: String): List<RecommendedMission> = listOf(
    mission("Kuliah", field, 0, "Review modul $field 30 menit", "Setiap Hari", 30),
    mission("Kuliah", field, 1, "Kerjakan latihan atau tugas $field", "Setiap Hari", 50),
    mission("Kuliah", field, 2, "Susun rangkuman perkuliahan $field", "3x Seminggu", 30),
    mission("Kuliah", field, 3, "Update progres proyek atau riset $field", "Senin-Jumat", 50),
    mission("Kuliah", field, 4, "Baca referensi tambahan $field", "Setiap Hari", 20),
    mission("Kuliah", field, 5, "Rapikan folder materi $field", "3x Seminggu", 20),
    mission("Kuliah", field, 6, "Latihan presentasi topik $field", "3x Seminggu", 30),
    mission("Kuliah", field, 7, "Cek ulang deadline kelas $field", "Setiap Hari", 10)
)

private fun workFieldPack(field: String): List<RecommendedMission> = listOf(
    mission("Kerja", field, 0, "Deep work prioritas $field 45 menit", "Senin-Jumat", 50),
    mission("Kerja", field, 1, "Rapikan backlog atau dokumen $field", "Setiap Hari", 30),
    mission("Kerja", field, 2, "Kirim update progres $field", "Senin-Jumat", 20),
    mission("Kerja", field, 3, "Refleksi perbaikan kerja $field", "3x Seminggu", 30),
    mission("Kerja", field, 4, "Siapkan prioritas besok untuk $field", "Setiap Malam", 20),
    mission("Kerja", field, 5, "Review hasil kerja $field", "Senin-Jumat", 30),
    mission("Kerja", field, 6, "Rapikan catatan meeting $field", "Setiap Hari", 20),
    mission("Kerja", field, 7, "Belajar satu insight baru untuk $field", "3x Seminggu", 30)
)

private fun mission(
    activityType: String,
    field: String,
    index: Int,
    title: String,
    schedule: String,
    weight: Int
): RecommendedMission = RecommendedMission(
    id = buildMissionId(activityType, field, index),
    activityType = activityType,
    field = field,
    title = title,
    schedule = schedule,
    weight = weight,
    reason = "Rekomendasi untuk $activityType bidang $field supaya fokus, tugas utama, dan progres produktivitas lebih terarah."
)

private fun buildMissionId(activityType: String, field: String, index: Int): String {
    val slug = "$activityType-$field"
        .lowercase(Locale.US)
        .replace(" ", "-")
        .replace("/", "-")
    return "$slug-$index"
}
