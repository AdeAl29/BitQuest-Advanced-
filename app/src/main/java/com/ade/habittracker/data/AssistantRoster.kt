package com.ade.habittracker.data

import com.ade.habittracker.model.AppData
import com.ade.habittracker.model.isCompletedOn
import com.ade.habittracker.model.isDueOn
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

data class AssistantPersona(
    val id: String,
    val name: String,
    val archetype: String,
    val tagline: String,
    val assetName: String,
    val greeting: String,
    val quickPrompts: List<String>,
    val moodLabel: String
)

object AssistantRoster {
    const val DEFAULT_ID = "aika_tsundere"

    val ITEMS = listOf(
        AssistantPersona(
            id = "aika_tsundere",
            name = "Aika",
            archetype = "Tsundere Coach",
            tagline = "Tajam di awal, tapi diam-diam paling perhatian sama progresmu.",
            assetName = "assistant_aika",
            greeting = "Hmph... aku cuma mampir buat memastikan misimu nggak berantakan. Kalau mau, aku bisa bantu pilih langkah paling masuk akal dulu.",
            quickPrompts = listOf(
                "Tolong omelin aku biar gerak",
                "Pilihkan misi paling penting",
                "Kasih target kecil dulu"
            ),
            moodLabel = "tegas"
        ),
        AssistantPersona(
            id = "luna_kuudere",
            name = "Luna",
            archetype = "Kuudere Analyst",
            tagline = "Kalem, rapi, dan paling jago menyusun langkah tanpa drama.",
            assetName = "assistant_luna",
            greeting = "Aku sudah melihat ritme kerjamu. Kalau kita bicara pelan dan rapi, progresmu bisa naik tanpa bikin kepala penuh.",
            quickPrompts = listOf(
                "Bantu aku fokus pelan-pelan",
                "Tolong rapikan prioritas",
                "Bikin rencana singkat"
            ),
            moodLabel = "dingin elegan"
        ),
        AssistantPersona(
            id = "sora_genki",
            name = "Sora",
            archetype = "Genki Cheerleader",
            tagline = "Energinya tinggi, paling jago bikin suasana jadi ringan dan bergerak.",
            assetName = "assistant_sora",
            greeting = "Yosh! Kita nggak perlu nunggu mood sempurna. Satu misi kecil dulu juga sudah keren banget buat mulai hari ini!",
            quickPrompts = listOf(
                "Semangatin aku dong",
                "Kasih misi ringan dulu",
                "Bikin aku mood ngerjain"
            ),
            moodLabel = "ceria"
        ),
        AssistantPersona(
            id = "elara_strategist",
            name = "Elara",
            archetype = "Strategist Onee-san",
            tagline = "Tenang, dewasa, dan suka mengubah target besar jadi langkah yang elegan.",
            assetName = "assistant_elara",
            greeting = "Tenang saja. Target besar tidak harus dikejar dengan panik. Kita pecah jadi langkah kecil yang terlihat anggun dan tetap efektif.",
            quickPrompts = listOf(
                "Susun strategi hari ini",
                "Bagi tugas besar jadi kecil",
                "Bantu evaluasi progres"
            ),
            moodLabel = "anggun"
        ),
        AssistantPersona(
            id = "yuna_healer",
            name = "Yuna",
            archetype = "Healing Companion",
            tagline = "Lembut, suportif, dan enak diajak bangkit saat energi sedang turun.",
            assetName = "assistant_yuna",
            greeting = "Kalau hari ini terasa berat, tidak apa-apa. Kita cari langkah yang paling ringan dulu, lalu biarkan ritme baikmu balik pelan-pelan.",
            quickPrompts = listOf(
                "Aku lagi capek",
                "Tolong kasih langkah paling ringan",
                "Bantu aku bangkit pelan"
            ),
            moodLabel = "lembut"
        ),
        AssistantPersona(
            id = "reina_gamer",
            name = "Reina",
            archetype = "Playful Gamer",
            tagline = "Ngelihat produktivitas kayak game: XP, streak, combo, dan clean clear.",
            assetName = "assistant_reina",
            greeting = "Oke, player. Kita anggap hari ini satu run yang bagus. Ambil combo kecil dulu, kumpulin XP, lalu sapu misi satu-satu sampai clean clear.",
            quickPrompts = listOf(
                "Kasih aku mode speedrun",
                "Misi mana yang kasih XP paling worth",
                "Bikin hari ini terasa seru"
            ),
            moodLabel = "playful"
        )
    )

    fun getById(id: String): AssistantPersona? = ITEMS.firstOrNull { it.id == id }
}

private data class AssistantReplySnapshot(
    val assistant: AssistantPersona,
    val topic: String,
    val seed: String,
    val userName: String,
    val dayName: String,
    val prettyDate: String,
    val prettyTime: String,
    val greetingSlot: String,
    val level: Int,
    val coins: Int,
    val streak: Int,
    val totalXp: Int,
    val totalHabitsCompleted: Int,
    val dueToday: Int,
    val completedToday: Int,
    val pendingToday: Int,
    val focusMinutes: Int,
    val focusToday: Int,
    val focusTarget: Int,
    val focusBestStreak: Int,
    val topPendingHabit: String,
    val urgentHabitName: String,
    val easiestHabitName: String,
    val highestRewardHabitName: String,
    val focusCandidateHabitName: String,
    val topCompletedHabit: String,
    val dueHabitPreview: String,
    val completedHabitPreview: String,
    val pendingPriorityPreview: String,
    val profileMode: String,
    val soundtrackCount: Int,
    val currentSoundtrackLabel: String,
    val messageSnippet: String
)

fun generateAssistantReply(
    assistant: AssistantPersona,
    userMessage: String,
    appData: AppData
): String {
    val lower = userMessage.lowercase(Locale.forLanguageTag("id-ID"))
    val today = LocalDate.now()
    val now = LocalTime.now()
    val locale = Locale.forLanguageTag("id-ID")
    val dueToday = appData.habits.count { it.isDueOn(today) }
    val completedToday = appData.habits.count { it.isCompletedOn(today) }
    val pendingToday = (dueToday - completedToday).coerceAtLeast(0)
    val focusMinutes = appData.focusTotalMinutes
    val focusToday = appData.focusSessionsCompletedToday
    val streak = appData.streak
    val level = appData.level
    val coins = appData.coins
    val username = appData.userName.ifBlank { "Petualang" }
    val totalHabitsCompleted = appData.totalHabitsCompleted
    val prettyDate = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale))
    val prettyTime = now.format(DateTimeFormatter.ofPattern("HH:mm", locale))
    val currentGreeting = when {
        now.hour < 11 -> "pagi"
        now.hour < 15 -> "siang"
        now.hour < 18 -> "sore"
        else -> "malam"
    }

    val topic = when {
        lower.contains("hari ini hari apa") ||
            lower.contains("hari apa sekarang") ||
            lower.contains("hari apa ini") ||
            lower.contains("tanggal berapa") ||
            lower.contains("tanggal hari ini") -> "date"
        lower.contains("jam berapa") ||
            lower.contains("sekarang jam berapa") ||
            lower.contains("pukul berapa") -> "time"
        lower.contains("siapa kamu") ||
            lower.contains("nama kamu") ||
            lower.contains("namamu") -> "identity"
        lower.contains("apa kabar") ||
            lower.contains("gimana kabar") ||
            lower.contains("gimana keadaan") -> "status"
        lower.contains("aku siapa") ||
            lower.contains("namaku siapa") ||
            lower.contains("profilku") -> "profile"
        lower.contains("koin") ||
            lower.contains("coin") ||
            lower.contains("uang") ||
            lower.contains("toko") -> "coins"
        lower.contains("kalender") ||
            lower.contains("jadwal") ||
            lower.contains("hari ini ada apa") -> "calendar"
        lower.contains("mulai dari mana") ||
            lower.contains("harus ngapain") ||
            lower.contains("kerjain apa dulu") ||
            lower.contains("saran") ||
            lower.contains("rekomendasi") -> "recommendation"
        lower.contains("semangat") ||
            lower.contains("motivasi") ||
            lower.contains("dorong") -> "motivation"
        lower.contains("capek") || lower.contains("lelah") || lower.contains("mager") || lower.contains("malas") -> "tired"
        lower.contains("fokus") || lower.contains("pomodoro") || lower.contains("timer") -> "focus"
        lower.contains("misi") || lower.contains("tugas") || lower.contains("habit") -> "mission"
        lower.contains("streak") || lower.contains("konsisten") -> "streak"
        lower.contains("xp") || lower.contains("level") -> "level"
        lower.contains("halo") || lower.contains("hai") || lower.contains("hi") -> "greet"
        lower.contains("makasih") || lower.contains("terima kasih") -> "thanks"
        else -> "general"
    }

    val profileMode = listOfNotNull(
        appData.recommendationActivityType.takeIf { it.isNotBlank() },
        appData.recommendationField.takeIf { it.isNotBlank() },
        appData.recommendationStage.takeIf { it.isNotBlank() }
    ).joinToString(" - ").ifBlank { "ritme produktifmu" }

    val dueHabitsToday = appData.habits.filter { it.isDueOn(today) }
    val pendingHabitsToday = dueHabitsToday.filterNot { it.isCompletedOn(today) }
    val urgentHabit = pendingHabitsToday.maxByOrNull { it.weight }
    val easiestHabit = pendingHabitsToday.minByOrNull { it.weight }
    val focusCandidate = pendingHabitsToday
        .sortedWith(compareByDescending<com.ade.habittracker.model.Habit> { it.weight }.thenBy { it.name.length })
        .firstOrNull()
    val priorityPreview = pendingHabitsToday
        .sortedByDescending { it.weight }
        .take(3)
        .joinToString(", ") { it.name }
        .ifBlank { "belum ada prioritas yang kebuka" }

    val snapshot = AssistantReplySnapshot(
        assistant = assistant,
        topic = topic,
        seed = "${assistant.id}|$topic|$userMessage|$pendingToday|$completedToday|$level|$streak|$focusToday|$coins",
        userName = username,
        dayName = today.format(DateTimeFormatter.ofPattern("EEEE", locale)),
        prettyDate = prettyDate,
        prettyTime = prettyTime,
        greetingSlot = currentGreeting,
        level = level,
        coins = coins,
        streak = streak,
        totalXp = appData.totalXp,
        totalHabitsCompleted = totalHabitsCompleted,
        dueToday = dueToday,
        completedToday = completedToday,
        pendingToday = pendingToday,
        focusMinutes = focusMinutes,
        focusToday = focusToday,
        focusTarget = appData.focusDailyTargetSessions,
        focusBestStreak = appData.focusBestStreakDays,
        topPendingHabit = pendingHabitsToday.firstOrNull()?.name
            ?: dueHabitsToday.firstOrNull()?.name
            ?: "satu misi kecil",
        urgentHabitName = urgentHabit?.name ?: "satu misi utama",
        easiestHabitName = easiestHabit?.name ?: "langkah paling ringan",
        highestRewardHabitName = urgentHabit?.name ?: "misi paling bernilai",
        focusCandidateHabitName = focusCandidate?.name ?: "satu target fokus",
        topCompletedHabit = appData.habits.lastOrNull { it.isCompletedOn(today) }?.name
            ?: "belum ada misi yang selesai",
        dueHabitPreview = dueHabitsToday.take(3).joinToString(", ") { it.name }
            .ifBlank { "belum ada misi terjadwal" },
        completedHabitPreview = appData.habits.filter { it.isCompletedOn(today) }.takeLast(3).joinToString(", ") { it.name }
            .ifBlank { "belum ada misi yang selesai" },
        pendingPriorityPreview = priorityPreview,
        profileMode = profileMode,
        soundtrackCount = appData.selectedSoundtracks.size,
        currentSoundtrackLabel = SoundtrackLibrary.getById(appData.currentSoundtrack)?.name ?: "Soundtrack utama",
        messageSnippet = userMessage.trim().removePrefix("aku ").take(64)
    )

    val composed = listOfNotNull(
        topicLeadLine(snapshot),
        topicBodyLine(snapshot),
        specialtyLine(snapshot),
        topicActionLine(snapshot),
        personalNoteLine(snapshot)
    ).joinToString(" ")

    return styleReply(
        assistant = assistant,
        topic = topic,
        seed = snapshot.seed,
        baseReply = composed
    )
}

private fun styleReply(
    assistant: AssistantPersona,
    topic: String,
    seed: String,
    baseReply: String
): String {
    val opener = when (assistant.id) {
        "aika_tsundere" -> pickVariant(
            "$seed|open",
            "Hmph... ",
            "Ya sudah, dengar dulu. ",
            "Oke, aku jawab yang jelas. ",
            "Kalau kamu memang mau serius, begini. ",
            "Aku bantu, tapi jangan pakai mode malas. ",
            "Fokus dulu sebentar. "
        )
        "luna_kuudere" -> pickVariant(
            "$seed|open",
            "",
            "Baik. ",
            "Kita lihat dengan tenang. ",
            "Aku susun yang rapi dulu. ",
            "Kalau dibaca pelan, jawabannya begini. ",
            "Oke, ini versi yang paling presisi. "
        )
        "sora_genki" -> pickVariant(
            "$seed|open",
            "Sip, denger ya. ",
            "Yosh, gini. ",
            "Okeee, kita bahas. ",
            "Ayo, aku bantu sekarang. ",
            "Gas, ini gampang kok. ",
            "Mantap, kita urai pelan-pelan. "
        )
        "elara_strategist" -> pickVariant(
            "$seed|open",
            "Baik, mari kita susun. ",
            "Tenang, ini bisa dijelaskan rapi. ",
            "Aku ambil inti yang paling berguna dulu. ",
            "Kalau kita baca dengan kepala dingin, begini. ",
            "Mari kita lihat dengan struktur yang jelas. ",
            "Aku bantu pecahkan jadi bentuk yang lebih enak dipakai. "
        )
        "yuna_healer" -> pickVariant(
            "$seed|open",
            "Iya, aku dengerin. ",
            "Tenang ya, kita bahas. ",
            "Pelan-pelan, aku bantu. ",
            "Tidak apa-apa, kita lihat satu per satu. ",
            "Aku ada di sini, jadi santai saja dulu. ",
            "Yuk, kita buat ini terasa lebih ringan. "
        )
        "reina_gamer" -> pickVariant(
            "$seed|open",
            "Oke player, briefing singkat. ",
            "Sip, kita cek map-nya. ",
            "Gas, aku kasih walkthrough kecil. ",
            "Oke, kita baca situasinya dulu. ",
            "Ready? Gini setup-nya. ",
            "Yes, ini jalur paling aman buat run-mu. "
        )
        else -> ""
    }

    val closer = when (assistant.id) {
        "aika_tsundere" -> pickVariant(
            "$seed|close",
            " Jangan kebanyakan alasan, ya.",
            " Sekarang tinggal gerak sedikit.",
            " Aku lebih suka lihat progres daripada drama.",
            " Pokoknya mulai dulu.",
            " Kamu bisa, cuma jangan kebanyakan menunda.",
            " Niat saja tidak bikin XP nambah."
        )
        "luna_kuudere" -> pickVariant(
            "$seed|close",
            " Cukup satu langkah presisi setelah ini.",
            " Tidak perlu ramai, cukup konsisten.",
            " Pelan itu boleh, asal tetap bergerak.",
            " Jaga ritmenya tetap tenang.",
            " Fokus pada satu hal dulu.",
            " Sisanya akan lebih mudah kalau awalnya rapi."
        )
        "sora_genki" -> pickVariant(
            "$seed|close",
            " Ayo ambil satu kemenangan kecil sekarang.",
            " Begitu mulai, mood biasanya ikut bangun.",
            " Kita gas pelan tapi jadi.",
            " Satu clear kecil dulu, lalu lihat apakah mau lanjut.",
            " Aku dukung penuh, jadi jangan takut mulai.",
            " Hari ini masih bisa dibikin keren."
        )
        "elara_strategist" -> pickVariant(
            "$seed|close",
            " Satu keputusan baik bisa merapikan seluruh harimu.",
            " Jaga langkah berikutnya tetap sederhana dan selesai.",
            " Kita tidak perlu terburu-buru, hanya tepat sasaran.",
            " Struktur kecil yang rapi biasanya membawa hasil besar.",
            " Biar harimu terasa elegan, cukup bergerak dengan arah jelas.",
            " Lanjutkan tanpa panik."
        )
        "yuna_healer" -> pickVariant(
            "$seed|close",
            " Ambil langkah yang paling ramah untuk dirimu dulu.",
            " Tidak perlu sempurna untuk tetap berarti.",
            " Pelan bukan masalah, yang penting kamu tetap hadir buat dirimu sendiri.",
            " Tarik napas, lalu pilih satu langkah lembut.",
            " Hari ini masih bisa baik meski dimulai kecil.",
            " Aku temani pelan-pelan."
        )
        "reina_gamer" -> pickVariant(
            "$seed|close",
            " Anggap saja ini satu move bagus buat jaga combo.",
            " Cari satu clear kecil lalu lanjut jika energinya masih enak.",
            " Build momentumnya dulu, baru push objective berikutnya.",
            " Yang penting run-nya tetap hidup.",
            " Satu step rapi lebih worth daripada spam gerakan.",
            " Kita main pintar, bukan asal cepat."
        )
        else -> ""
    }

    val topicTag = when (topic) {
        "thanks" -> ""
        "greet" -> ""
        else -> pickVariant(
            "$seed|tag",
            "",
            "",
            " ",
            " ",
            " "
        )
    }

    return "$opener$baseReply$topicTag$closer"
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun pickVariant(seed: String, vararg options: String): String {
    if (options.isEmpty()) return ""
    val index = (seed.hashCode().absoluteValue) % options.size
    return options[index]
}

private fun topicLeadLine(snapshot: AssistantReplySnapshot): String {
    val options = when (snapshot.topic) {
        "date" -> arrayOf(
            "Hari ini ${snapshot.prettyDate}.",
            "Sekarang ${snapshot.prettyDate}.",
            "Kalau yang kamu cari hari dan tanggal, ini ${snapshot.prettyDate}.",
            "Hari ini ${snapshot.dayName}, tepatnya ${snapshot.prettyDate}.",
            "Tanggalnya sekarang ${snapshot.prettyDate}."
        )
        "time" -> arrayOf(
            "Sekarang pukul ${snapshot.prettyTime}.",
            "Jam sekarang ${snapshot.prettyTime}.",
            "Kalau lihat waktunya, sekarang ${snapshot.prettyTime}.",
            "Waktu di sini menunjukkan ${snapshot.prettyTime}.",
            "Sekarang sudah jam ${snapshot.prettyTime}."
        )
        "identity" -> arrayOf(
            "Aku ${snapshot.assistant.name}, ${snapshot.assistant.archetype.lowercase()}-mu di BitQuest.",
            "Namaku ${snapshot.assistant.name}, partner chat kamu di BitQuest.",
            "Aku ${snapshot.assistant.name}, assistant yang memang dibuat buat nemenin progresmu.",
            "Kalau ditanya siapa aku, aku ${snapshot.assistant.name} dan tugasku jadi teman mikirmu di sini.",
            "Aku ${snapshot.assistant.name}, bagian dari tim assistant BitQuest yang siap nemenin ritmemu."
        )
        "status" -> arrayOf(
            "Aku baik dan siap bantu.",
            "Kondisiku aman dan fokus.",
            "Aku oke, tinggal lihat ritmemu hari ini.",
            "Aku siap nemenin kamu ngobrol atau nyusun langkah.",
            "Aku baik, dan sekarang fokusku ada di kamu."
        )
        "profile" -> arrayOf(
            "Aku cek profilmu barusan.",
            "Kalau lihat profilmu sekarang, datanya cukup jelas.",
            "Profil BitQuest-mu sudah kebaca lumayan rapi.",
            "Dari profilmu, arah progresnya mulai kelihatan.",
            "Data profilmu sekarang cukup enak dibaca."
        )
        "coins" -> arrayOf(
            "Soal koin, aku lihat angkanya dulu.",
            "Untuk urusan resource, posisi koinmu cukup jelas.",
            "Aku cek tabungan koinmu dulu.",
            "Kalau bahas koin, datanya kebaca begini.",
            "Oke, aku lihat stok koinmu."
        )
        "calendar" -> arrayOf(
            "Kalau lihat jadwal hari ini, polanya cukup jelas.",
            "Aku cek kalender misimu dulu.",
            "Dari ritme hari ini, daftar misimu masih kebaca sehat.",
            "Kalendermu hari ini memberi cukup petunjuk.",
            "Jadwal hari ini tidak kosong, jadi kita punya pegangan."
        )
        "recommendation" -> arrayOf(
            "Kalau kamu bingung mulai dari mana, ada beberapa jalur yang paling masuk akal.",
            "Aku bisa potong kebingunganmu jadi langkah pertama.",
            "Kita tidak perlu buka semua hal sekaligus.",
            "Versi paling aman adalah mulai dari satu target yang jelas.",
            "Aku sudah punya arah pertama buatmu."
        )
        "motivation" -> arrayOf(
            "Kalau kamu butuh semangat, aku kasih yang realistis dulu.",
            "Kamu tidak butuh pidato panjang, cuma pijakan yang pas buat mulai.",
            "Motivasi itu bisa dinyalakan, tidak harus ditunggu.",
            "Kalau lagi butuh dorongan, kita isi secukupnya dulu.",
            "Semangat hari ini masih bisa dibangun dari hal kecil."
        )
        "tired" -> arrayOf(
            "Kalau kamu lagi capek, targetnya memang harus ikut berubah.",
            "Aku dengar kamu lagi lelah.",
            "Saat energi tipis, ritmenya jangan dipaksa keras.",
            "Kalau badan atau pikiranmu turun, kita ubah pendekatannya.",
            "Capek itu valid, tinggal jangan biarkan harimu lepas total."
        )
        "focus" -> arrayOf(
            "Soal fokus, datamu masih bisa dibaca cukup jelas.",
            "Aku cek sesi fokusmu dulu.",
            "Untuk mode fokus, ritmemu sekarang begini.",
            "Kalau bicara pomodoro, kamu masih punya ruang bagus buat lanjut.",
            "Fokus hari ini masih bisa dirapikan."
        )
        "mission" -> arrayOf(
            "Kalau bicara misi, daftar harimu sebenarnya kasih petunjuk.",
            "Aku lihat daftar misimu dulu.",
            "Misi hari ini tidak seberantakan yang kamu kira.",
            "Daftar misimu sekarang punya prioritas yang bisa dibaca.",
            "Quest hari ini masih bisa disusun dengan sehat."
        )
        "streak" -> arrayOf(
            "Streak-mu sekarang cukup bicara banyak.",
            "Kalau bahas konsistensi, nilainya tidak kecil.",
            "Streak itu kecil di tampilan, tapi besar efeknya.",
            "Angka streak-mu punya cerita sendiri.",
            "Aku lihat streak-mu sebagai sinyal kebiasaan yang lagi tumbuh."
        )
        "level" -> arrayOf(
            "Level dan XP-mu sekarang sudah menunjukkan arah perkembangan.",
            "Kalau dilihat dari level, kamu sudah bergerak lumayan jauh.",
            "Progress level-mu sedang membentuk identitas kerja yang jelas.",
            "XP dan level sekarang bukan angka kosong.",
            "Data level-mu cukup enak dibaca."
        )
        "thanks" -> arrayOf(
            "Sama-sama.",
            "Sip, senang kalau kepakai.",
            "Aman.",
            "Tidak masalah.",
            "Santai, aku senang bisa bantu."
        )
        "greet" -> arrayOf(
            "Halo, selamat ${snapshot.greetingSlot}.",
            "Hai juga, selamat ${snapshot.greetingSlot}.",
            "Halo ${snapshot.userName}, selamat ${snapshot.greetingSlot}.",
            "Hai, aku siap nemenin ${snapshot.greetingSlot} ini.",
            "Selamat ${snapshot.greetingSlot}, aku di sini."
        )
        else -> arrayOf(
            "Aku tangkap inti pertanyaanmu.",
            "Oke, aku coba baca maksudmu dulu.",
            "Kalau aku simpulkan, kamu lagi butuh arah yang lebih enak dicerna.",
            "Aku bisa jawab dari sisi yang paling berguna dulu.",
            "Mari kita ubah ini jadi langkah yang lebih jelas."
        )
    }
    return pickVariant("${snapshot.seed}|lead", *options)
}

private fun topicBodyLine(snapshot: AssistantReplySnapshot): String {
    val options = when (snapshot.topic) {
        "date" -> arrayOf(
            "Artinya kamu masih punya satu hari yang utuh buat bikin progres kecil terasa nyata.",
            "Ini momen yang bagus buat reset kepala dan mulai dari titik yang jelas.",
            "Dengan tahu harinya, kamu bisa pasang ekspektasi yang lebih realistis ke dirimu sendiri.",
            "Hari ini masih cukup segar buat nambah satu aksi yang benar-benar selesai.",
            "Sekarang tinggal tentukan apakah hari ini dipakai buat dorong misi penting atau rapihin ritme dulu."
        )
        "time" -> arrayOf(
            "Jam segini masih cukup oke buat satu misi ringan atau satu sesi fokus pendek.",
            "Kalau belum mulai apa-apa, masih ada ruang buat satu langkah kecil yang jelas ujungnya.",
            "Waktu sekarang bukan soal terlambat atau tidak, tapi soal langkah paling waras yang bisa kamu ambil.",
            "Masih ada kesempatan buat satu keputusan baik sebelum harimu geser ke hal lain.",
            "Kalau kamu mau jaga ritme, satu task sederhana di jam ini sudah bernilai."
        )
        "identity" -> arrayOf(
            "Peranku bukan cuma menjawab, tapi juga bantu kamu tetap bergerak sesuai gaya yang paling cocok buatmu.",
            "Setiap assistant di sini punya cara bicara dan pola support berbeda, jadi kamu bisa pilih yang paling klik.",
            "Tujuanku sederhana: bikin misi, fokus, dan progresmu terasa lebih gampang disentuh.",
            "Aku memang dibikin supaya obrolanmu tidak terasa datar dan bantuanku lebih terasa personal.",
            "Kalau kamu sering ngobrol, aku bisa membaca pola pertanyaanmu dengan lebih rapi."
        )
        "status" -> arrayOf(
            "Hari ini kamu sudah menyelesaikan ${snapshot.completedToday} dari ${snapshot.dueToday} misi yang terjadwal.",
            "Dari data app, masih ada ${snapshot.pendingToday} ruang progres yang bisa diselamatkan hari ini.",
            "Sekarang ritmemu lebih condong ke ${snapshot.profileMode}, jadi aku jawab dengan nada yang paling pas ke situ.",
            "Di level ${snapshot.level} dengan streak ${snapshot.streak} hari, fondasimu sebenarnya tidak buruk sama sekali.",
            "Aku suka karena datamu masih kasih cukup pegangan buat bantu secara konkret."
        )
        "profile" -> arrayOf(
            "Kamu sekarang level ${snapshot.level}, streak ${snapshot.streak} hari, total XP ${snapshot.totalXp}, dan total misi selesai ${snapshot.totalHabitsCompleted}.",
            "Profilmu kelihatan seperti orang yang sedang membangun ${snapshot.profileMode} dengan cukup serius.",
            "Ada jejak kerja yang jelas di akunmu: ${snapshot.totalHabitsCompleted} misi selesai dan ${snapshot.focusMinutes} menit fokus terkumpul.",
            "Secara angka, kamu tidak mulai dari nol lagi. Progresmu sudah punya tubuh yang terasa nyata.",
            "Kalau profil dibaca sebagai pola, kamu lebih cocok dorong konsistensi daripada ledakan besar lalu hilang."
        )
        "coins" -> arrayOf(
            "Koinmu sekarang ada ${snapshot.coins}, jadi kamu bisa pilih mau simpan dulu atau pakai buat hal yang benar-benar bikin akunmu terasa lebih personal.",
            "Jumlah koin saat ini ${snapshot.coins}, jadi keputusan belanja paling bagus adalah yang tidak menukar progres utama dengan impuls sesaat.",
            "Dengan ${snapshot.coins} koin, kamu masih aman kalau mau sabar sedikit sambil nambah hasil dari misi dan fokus.",
            "Aku melihat koin itu sebagai bonus dari ritme, jadi jangan sampai ngorbanin momentum cuma demi buru-buru belanja.",
            "Stok koinmu ${snapshot.coins}; cukup buat jadi pengingat bahwa kerja kecilmu memang ada hasilnya."
        )
        "calendar" -> arrayOf(
            "Hari ini ada ${snapshot.dueToday} misi terjadwal, ${snapshot.completedToday} sudah kelar, dan yang masih kebuka ${snapshot.pendingToday}.",
            "Daftar hari ini berisi ${snapshot.dueHabitPreview}.",
            "Kalendermu sebenarnya tidak kosong, jadi tinggal dipilih bagian mana yang mau disentuh lebih dulu.",
            "Kalau dibaca dari ritme harian, jadwalmu cukup sehat untuk dikerjakan bertahap.",
            "Hari ini bukan soal menghabisi semuanya, tapi memilih urutan yang tidak bikin kepalamu penuh."
        )
        "recommendation" -> arrayOf(
            "Kalau aku harus menunjuk satu langkah awal, aku akan pilih ${snapshot.urgentHabitName} sebagai pemantik utama dan ${snapshot.easiestHabitName} sebagai fallback yang aman.",
            "Langkah pertama terbaik biasanya yang cukup ringan buat mulai, tapi tetap bikin lega setelah selesai; untukmu itu dekat ke ${snapshot.easiestHabitName}.",
            "Daripada mikir seluruh daftar, sentuh dulu ${snapshot.urgentHabitName} lalu lihat apakah momentummu naik.",
            "Aku lebih suka kamu buka tugas yang paling jelas ujungnya dulu, dan sekarang kandidat utamanya ${snapshot.urgentHabitName}.",
            "Versi paling waras buat mulai adalah mengambil satu target yang tidak terlalu besar tapi langsung terasa hasilnya, lalu pindah ke ${snapshot.urgentHabitName}."
        )
        "motivation" -> arrayOf(
            "Kamu tidak butuh jadi luar biasa dulu untuk mulai; kamu cuma butuh satu aksi yang benar-benar terjadi.",
            "Motivasi yang paling kuat biasanya datang sesudah gerak pertama, bukan sebelumnya.",
            "Progres kecil tetap menggeser hidupmu ke arah yang lebih baik, meski kelihatannya sepele di awal.",
            "Saat kamu bergerak sedikit, otakmu dapat bukti bahwa hari ini belum hilang.",
            "Semangat yang stabil jauh lebih berharga daripada ledakan besar yang cuma bertahan sebentar."
        )
        "tired" -> arrayOf(
            "Kalau capek, kurangi targetnya sampai tubuhmu bilang 'ini masih mungkin'.",
            "Jangan paksa performa penuh. Kita cukup cari langkah yang lembut tapi nyata.",
            "Saat energi turun, kemenangan kecil justru jadi lebih penting daripada biasanya.",
            "Yang kamu butuhkan mungkin bukan disiplin keras, tapi versi tugas yang lebih ramah.",
            "Capek bukan berarti gagal; itu cuma tanda kalau ritme hari ini harus dibuat lebih manusiawi."
        )
        "focus" -> arrayOf(
            "Total fokusmu sudah ${snapshot.focusMinutes} menit, dengan ${snapshot.focusToday} sesi hari ini dari target ${snapshot.focusTarget} sesi.",
            "Data fokusmu menunjukkan kamu masih bisa menambah satu sesi pendek tanpa bikin hari terasa berat.",
            "Kalau targetmu menjaga ritme, sesi fokus singkat sering lebih berguna daripada menunggu mood panjang.",
            "Focus streak terbaikmu ${snapshot.focusBestStreak} hari, jadi kamu sudah pernah membuktikan bisa stabil.",
            "Potensi fokusmu ada, tinggal dikunci ke satu target seperti ${snapshot.focusCandidateHabitName} dan jangan dibelah ke banyak arah."
        )
        "mission" -> arrayOf(
            "Misi paling dekat yang bisa disentuh sekarang adalah ${snapshot.topPendingHabit}, tapi yang paling layak dijaga prioritasnya ${snapshot.urgentHabitName}.",
            "Dari daftar hari ini, yang sudah selesai antara lain ${snapshot.completedHabitPreview}.",
            "Masih ada ${snapshot.pendingToday} misi terbuka, jadi jelas masih ada ruang buat bikin harimu terasa menang.",
            "Kalau kamu ingin cepat merasa bergerak, pilih ${snapshot.easiestHabitName} dulu lalu dorong ${snapshot.urgentHabitName}.",
            "Daftar misi hari ini tidak perlu kamu taklukkan sekaligus; cukup buat urutan yang sehat seperti ${snapshot.pendingPriorityPreview}."
        )
        "streak" -> arrayOf(
            "Streak ${snapshot.streak} hari itu bukan angka pajangan, itu bukti kamu bisa muncul berulang kali.",
            "Nilai streak-mu sekarang ada di konsistensi kecil yang terus hidup, bukan di gengsinya.",
            "Menjaga streak sering kali cuma butuh satu tindakan waras di hari yang berat.",
            "Kalau streak-mu dijaga dengan langkah kecil, dia jadi lebih tahan lama daripada dipaksa dengan target besar.",
            "Aku suka melihat streak sebagai tali yang menghubungkan versi kamu kemarin, hari ini, dan besok."
        )
        "level" -> arrayOf(
            "Level ${snapshot.level} dengan total XP ${snapshot.totalXp} berarti kamu sudah menumpuk cukup banyak bukti kerja.",
            "Naik level di BitQuest memang pelan, tapi justru itu yang bikin progresmu terasa jujur.",
            "Semakin tinggi levelmu, semakin penting langkah yang rapi daripada langkah yang heboh.",
            "XP itu sebenarnya jejak dari keputusan-keputusan kecil yang kamu ulang dengan benar.",
            "Level sekarang menunjukkan kamu bukan pemula total lagi; ritmemu sudah mulai punya bentuk."
        )
        "thanks" -> arrayOf(
            "Kalau bantuanku bikin satu langkah jadi lebih jelas, berarti itu sudah cukup bagus.",
            "Yang penting sekarang bukan ucapannya, tapi langkah kecil berikutnya.",
            "Senang kalau jawabanku kepakai, apalagi kalau habis ini kamu langsung gerak sedikit.",
            "Aku terima, sekarang tinggal pertahankan momentumnya.",
            "Sip, lanjutkan ritmenya selagi masih hangat."
        )
        "greet" -> arrayOf(
            "Kalau kamu mau, kita bisa mulai dari ngobrol santai atau langsung masuk ke misi hari ini.",
            "Aku siap kalau kamu mau minta semangat, strategi, atau sekadar ditemani biar tidak terasa sendiri.",
            "Tinggal bilang mau aku jadi penyemangat, analis, atau partner yang cerewet sedikit.",
            "Hari ini kita bisa pilih mode ringan dulu atau langsung bahas target utamanya.",
            "Kalau kamu belum tahu mau ngomong apa, aku tetap bisa bantu baca kondisi akunmu dulu."
        )
        else -> arrayOf(
            "Pertanyaanmu tetap bisa diarahkan ke satu keputusan yang lebih enak dieksekusi.",
            "Kalau mau dibuat sederhana, kita tarik ini ke langkah yang bisa kamu sentuh sekarang.",
            "Aku bisa bantu mengubah kebingungan ini jadi keputusan kecil yang jelas.",
            "Biasanya yang kamu cari bukan jawaban besar, tapi titik mulai yang terasa mungkin.",
            "Aku rasa kamu butuh jawaban yang tidak muter-muter, jadi aku potong ke intinya."
        )
    }
    return pickVariant("${snapshot.seed}|body", *options)
}

private fun topicActionLine(snapshot: AssistantReplySnapshot): String {
    val options = when (snapshot.topic) {
        "date", "time", "calendar", "recommendation", "mission" -> arrayOf(
            "Kalau mau aman, buka ${snapshot.easiestHabitName} dulu lalu berhenti sebentar buat ukur energimu lagi sebelum pindah ke ${snapshot.urgentHabitName}.",
            "Pegang satu target dulu, jangan lebih dari itu, supaya otakmu tidak keburu riuh.",
            "Begitu satu item bergerak, baru putuskan apakah kamu mau lanjut ke ${snapshot.urgentHabitName} atau tutup sesi dengan rapi.",
            "Coba pilih ${snapshot.easiestHabitName} kalau kamu butuh start cepat, atau ${snapshot.urgentHabitName} kalau kamu mau langsung menyentuh inti hari ini.",
            "Ambil langkah pertama yang paling jelas ujungnya, lalu jadikan ${snapshot.urgentHabitName} sebagai target utama sesudahnya."
        )
        "motivation", "tired" -> arrayOf(
            "Boleh banget targetmu cuma lima sampai sepuluh menit asal benar-benar jadi aksi.",
            "Kalau berat, kecilkan medannya sampai kamu sanggup melangkah tanpa menahan napas.",
            "Kita tidak mengejar kesempurnaan, cuma bukti bahwa kamu masih mau hadir buat dirimu sendiri.",
            "Satu langkah lembut hari ini lebih menyelamatkan ritme daripada rencana besar yang tidak disentuh; untukmu versi lembut itu bisa mulai dari ${snapshot.easiestHabitName}.",
            "Ambil versi paling ramah dari tugasmu, lalu tutup dengan rasa cukup sebelum memikirkan yang lain."
        )
        "focus" -> arrayOf(
            "Sesi fokus pendek dengan satu target biasanya paling aman buat mengunci pikiranmu, dan kandidat terbaikmu sekarang ${snapshot.focusCandidateHabitName}.",
            "Kalau kamu bingung, set timer dan pasangkan dengan ${snapshot.focusCandidateHabitName} saja.",
            "Jangan campur banyak target dalam satu sesi, nanti fokusmu bocor ke mana-mana.",
            "Satu putaran fokus yang bersih sering lebih berharga daripada duduk lama sambil pindah-pindah tugas.",
            "Begitu timer mulai, jangan negosiasi lagi dengan dirimu sendiri."
        )
        else -> arrayOf(
            "Kalau kamu mau, habis ini aku bisa bantu lebih spesifik ke misi, fokus, atau strategi harianmu.",
            "Lempar saja pertanyaan berikutnya, aku bisa bikin jawabannya lebih tajam lagi.",
            "Kita bisa lanjut ke versi yang lebih praktis kalau kamu mau.",
            "Kalau butuh, aku bisa langsung potong ini jadi langkah yang lebih operasional.",
            "Aku siap kalau kamu mau pindah dari ngobrol ke eksekusi."
        )
    }
    return pickVariant("${snapshot.seed}|action", *options)
}

private fun personalNoteLine(snapshot: AssistantReplySnapshot): String? {
    val options = buildList {
        if (snapshot.pendingPriorityPreview != "belum ada prioritas yang kebuka") {
            add("Kalau aku urutkan cepat, kandidat teratasmu sekarang: ${snapshot.pendingPriorityPreview}.")
        }
        if (snapshot.pendingToday > 0) {
            add("Sekarang kamu masih punya ${snapshot.pendingToday} misi terbuka, jadi belum terlambat sama sekali buat menyelamatkan hari ini.")
        }
        if (snapshot.completedToday > 0) {
            add("Kamu juga sudah menuntaskan ${snapshot.completedToday} misi hari ini, jadi bukti geraknya sudah ada.")
        }
        if (snapshot.focusToday > 0) {
            add("Hari ini kamu sudah punya ${snapshot.focusToday} sesi fokus, jadi modal momentummu tidak nol.")
        }
        if (snapshot.streak > 0) {
            add("Streak ${snapshot.streak} hari itu layak dijaga dengan satu aksi kecil yang konsisten.")
        }
        if (snapshot.level >= 10) {
            add("Di level ${snapshot.level}, gaya mainmu sudah bukan pemula penuh lagi; kamu tinggal butuh ritme yang lebih rapi.")
        }
        if (snapshot.topCompletedHabit != "belum ada misi yang selesai") {
            add("Aku lihat ${snapshot.topCompletedHabit} sudah beres hari ini, itu sinyal bagus buat melanjutkan langkah berikutnya.")
        }
        if (snapshot.profileMode != "ritme produktifmu") {
            add("Karena profil rekomendasimu mengarah ke ${snapshot.profileMode}, aku bakal cenderung menyarankan langkah yang realistis buat jalur itu.")
        }
    }
    if (options.isEmpty()) return null
    val include = ("${snapshot.seed}|note-toggle".hashCode().absoluteValue) % 100 >= 35
    if (!include) return null
    return options[("${snapshot.seed}|note".hashCode().absoluteValue) % options.size]
}

private fun specialtyLine(snapshot: AssistantReplySnapshot): String {
    val options = when (snapshot.assistant.id) {
        "aika_tsundere" -> arrayOf(
            "Kalau kamu mau aku tegas, aku akan dorong ${snapshot.urgentHabitName} dulu dan berhenti memanjakan kebingunganmu.",
            "Spesialisasiku itu nendang kamu ke langkah yang paling penting, jadi untuk sekarang aku pasang ${snapshot.urgentHabitName} di depan.",
            "Aku paling cocok dipakai saat kamu mulai kebanyakan alasan, dan target yang harus kita jaga sekarang ${snapshot.urgentHabitName}.",
            "Buat mode disiplin, aku akan pakai ${snapshot.urgentHabitName} sebagai poros utama hari ini."
        )
        "luna_kuudere" -> arrayOf(
            "Kalau pakai gaya bantuanku, urutannya rapi: ${snapshot.easiestHabitName}, lalu ${snapshot.urgentHabitName}, baru sisanya.",
            "Aku lebih suka menyusun prioritas dengan tenang, dan list yang paling logis sekarang ${snapshot.pendingPriorityPreview}.",
            "Keahlianku memang di struktur, jadi aku akan membaca ${snapshot.urgentHabitName} sebagai inti dan ${snapshot.easiestHabitName} sebagai pembuka ritme.",
            "Kalau mau versi minim drama, pakai urutan yang bersih dari ${snapshot.pendingPriorityPreview}."
        )
        "sora_genki" -> arrayOf(
            "Kalau kamu butuh win cepat, aku bakal lempar ${snapshot.easiestHabitName} dulu biar mood-mu langsung kebangun.",
            "Aku paling jago nyari kemenangan kecil, jadi ${snapshot.easiestHabitName} itu kandidat manis buat pemanasan.",
            "Biar harimu cepat terasa hidup, aku suka mulai dari ${snapshot.easiestHabitName} lalu dorong ke target berikutnya.",
            "Mode ceria terbaikku itu bikin kamu bergerak dulu, dan pembuka paling aman sekarang ${snapshot.easiestHabitName}."
        )
        "elara_strategist" -> arrayOf(
            "Kalau pakai pendekatanku, ${snapshot.urgentHabitName} itu inti strateginya, lalu ${snapshot.easiestHabitName} jadi langkah pembukanya.",
            "Aku cenderung membaca ${snapshot.urgentHabitName} sebagai objective utama yang harus dijaga nilainya hari ini.",
            "Versi strategisku akan memecah hari ini jadi urutan: buka ritme dengan ${snapshot.easiestHabitName}, lalu rapikan ${snapshot.urgentHabitName}.",
            "Keahlianku ada di menata energi, jadi prioritas yang paling elegan sekarang berputar di ${snapshot.urgentHabitName}."
        )
        "yuna_healer" -> arrayOf(
            "Kalau energimu tidak penuh, aku akan memihak ke ${snapshot.easiestHabitName} dulu supaya kamu tidak langsung kehabisan napas.",
            "Gaya bantuku paling cocok buat hari yang lembek, jadi aku akan membuka ritme dari ${snapshot.easiestHabitName}.",
            "Aku lebih suka menjaga hatimu tetap aman, makanya ${snapshot.easiestHabitName} terasa lebih manusiawi buat disentuh dulu.",
            "Biar kamu tetap bergerak tanpa menyakiti diri sendiri, langkah awal yang paling ramah sekarang ${snapshot.easiestHabitName}."
        )
        "reina_gamer" -> arrayOf(
            "Kalau dilihat kayak game, ${snapshot.highestRewardHabitName} itu objective paling worth dan ${snapshot.easiestHabitName} itu combo starter-nya.",
            "Mode guide-ku akan bilang: buka combo dari ${snapshot.easiestHabitName}, lalu ambil clear bernilai di ${snapshot.highestRewardHabitName}.",
            "Aku paling suka membaca tugas sebagai run, dan target XP terbaikmu sekarang ${snapshot.highestRewardHabitName}.",
            "Kalau mau main efisien, pakai ${snapshot.easiestHabitName} buat warm-up lalu gas ke ${snapshot.highestRewardHabitName}."
        )
        else -> arrayOf("")
    }
    return pickVariant("${snapshot.seed}|specialty", *options)
}


