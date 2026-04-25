package com.ade.habittracker.notification

import kotlin.math.absoluteValue
import kotlin.random.Random

data class ReminderCopy(
    val title: String,
    val message: String
)

object ReminderMessageFactory {

    fun buildHabitReminder(
        displayName: String,
        habitName: String,
        habitWeight: Int,
        isSnooze: Boolean,
        seedHint: Int
    ): ReminderCopy {
        val shortName = trimLabel(habitName, 28)
        val random = seededRandom(seedHint, displayName, habitName, habitWeight, if (isSnooze) 1 else 0)
        val title = (if (isSnooze) snoozeTitles else habitTitles).random(random)
            .replace("{name}", displayName)
            .replace("{habit}", shortName)

        val message = habitBodies.random(random)
            .replace("{habit}", shortName)
            .replace("{xp}", habitWeight.toString())

        return ReminderCopy(title = title, message = message)
    }

    fun buildTimeSlotReminder(
        displayName: String,
        focusHabitName: String?,
        pendingCount: Int,
        forceNotify: Boolean,
        seedHint: Int
    ): ReminderCopy {
        val shortName = trimLabel(focusHabitName ?: "misi pilihanmu", 28)
        val random = seededRandom(seedHint, displayName, shortName, pendingCount, if (forceNotify) 7 else 3)

        if (forceNotify) {
            return ReminderCopy(
                title = enabledTitles.random(random).replace("{name}", displayName),
                message = enabledBodies.random(random)
            )
        }

        if (pendingCount <= 0) {
            return ReminderCopy(
                title = allDoneTitles.random(random).replace("{name}", displayName),
                message = allDoneBodies.random(random)
            )
        }

        val title = slotTitles.random(random)
            .replace("{name}", displayName)
            .replace("{habit}", shortName)

        val message = slotBodies.random(random)
            .replace("{habit}", shortName)
            .replace("{count}", pendingCount.toString())
            .replace("{others}", (pendingCount - 1).coerceAtLeast(0).toString())

        return ReminderCopy(title = title, message = message)
    }

    private fun seededRandom(vararg inputs: Any): Random {
        val seed = inputs.fold(17) { acc, value -> 31 * acc + value.hashCode() }
        return Random(seed.absoluteValue + System.currentTimeMillis().toInt())
    }

    private fun trimLabel(text: String, maxLength: Int): String {
        if (text.length <= maxLength) return text
        if (maxLength <= 1) return text.take(1)
        return text.take(maxLength - 1) + "..."
    }

    private val habitTitles = listOf(
        "🔥 Sedikit lagi, {name}",
        "✨ Saatnya gerak, {name}",
        "🚀 Yuk lanjut, {name}",
        "🌟 Biar hari ini tetap hidup, {name}",
        "💪 Gas pelan tapi jadi, {name}",
        "🎯 Fokus sebentar, {name}",
        "⚡ Momentum kamu lagi bagus, {name}",
        "🌈 Satu langkah lagi, {name}",
        "🧭 Arahmu sudah jelas, {name}",
        "🏆 Coba sentuh satu misi ini, {name}",
        "📌 Jangan biarkan ini lewat, {name}",
        "🌻 Hari ini masih bisa keren, {name}",
        "🪄 Bikin progres kecil dulu, {name}",
        "🎮 Main cantik dulu, {name}",
        "📈 Naik dikit juga tetap naik, {name}"
    )

    private val snoozeTitles = listOf(
        "🔁 Lanjut lagi, {name}",
        "⏳ Balik sebentar ke {habit}",
        "🌟 Waktunya sambung progres, {name}",
        "💫 Jangan putus di tengah, {name}",
        "🎯 Coba sekali lagi untuk {habit}",
        "⚡ Momentum belum habis, {name}",
        "🔥 Gas lagi buat {habit}",
        "🧩 Tinggal dirapikan sedikit, {name}"
    )

    private val slotTitles = listOf(
        "✨ Pilih satu dan gas, {name}",
        "🎯 Ada progres yang bisa kamu amankan, {name}",
        "🔥 Buka jalan dulu lewat {habit}",
        "🚀 Langkah kecil juga dihitung, {name}",
        "🌟 Mood produktif bisa dimulai sekarang, {name}",
        "📌 Coba mulai dari {habit}",
        "🧭 Arah hari ini masih aman, {name}",
        "💪 Yuk sentuh satu target dulu, {name}",
        "🏁 Sedikit progres lebih baik daripada kosong, {name}",
        "⚡ Curi satu kemenangan kecil, {name}"
    )

    private val enabledTitles = listOf(
        "🔔 Siap nemenin kamu, {name}",
        "✨ Pengingatmu sudah aktif, {name}",
        "🚀 Oke, kita jalan bareng ya",
        "🌟 Sip, notif produktif siap bantu",
        "🎯 Reminder sudah nyala, {name}"
    )

    private val allDoneTitles = listOf(
        "🎉 Aman semua, {name}",
        "🏆 Mantap, hari ini sudah kepegang",
        "✨ Rapi banget progresmu, {name}",
        "🌈 Hari ini kelihatan sehat, {name}",
        "💎 Misi slot ini sudah beres"
    )

    private val enabledBodies = listOf(
        "✨ Mulai sekarang aku bakal munculin dorongan kecil yang random, biar notifnya tidak terasa kaku lagi.",
        "🚀 Notif sudah siap bantu jaga ritme. Tinggal lanjut satu per satu, tanpa harus nunggu mood sempurna.",
        "🌟 Pengingat aktif. Kita bikin progres kecil yang konsisten, bukan drama produktif sesaat.",
        "🎯 Reminder dinyalakan. Biar ada yang narik kamu balik waktu mulai melambat.",
        "💡 Sip, sekarang tiap dorongan bakal datang dengan kalimat yang lebih hidup dan beda-beda."
    )

    private val allDoneBodies = listOf(
        "🎉 Tidak ada yang mendesak sekarang. Nikmati ritme yang sudah kamu jaga.",
        "✨ Bagian ini sudah aman. Kamu berhasil bikin hari berjalan rapi.",
        "🌿 Slot ini bersih dari beban. Pertahankan alurnya pelan-pelan.",
        "🏆 Semuanya sudah kelar untuk bagian ini. Itu bukan hal kecil.",
        "💎 Progress kamu lagi manis. Tidak perlu panik, tinggal jaga konsistensi.",
        "🌈 Hari ini kelihatan lebih ringan karena kamu sudah ngerapihin banyak hal.",
        "☁️ Tenang, yang tadi sempat nunggu sekarang sudah beres semua.",
        "🔥 Aman. Tinggal jaga ritme supaya tetap nyala sampai akhir hari."
    )

    private val habitBodies = listOf(
        "🔥 {habit} tinggal disentuh sedikit lagi. +{xp} XP sudah nunggu buat kamu.",
        "✨ Coba mulai dari {habit}. Langkah kecil ini bisa bikin hari tetap hidup.",
        "🚀 Tidak harus langsung besar, yang penting {habit} bergerak dulu sekarang.",
        "🌟 Sekali sentuh {habit}, progresmu langsung terasa lebih rapi.",
        "💪 {habit} bisa jadi kemenangan kecil yang bikin kamu lebih enak lanjut.",
        "🎯 Yuk beresin {habit} pelan-pelan. +{xp} XP sayang kalau dilewatkan.",
        "🧩 Kadang yang dibutuhkan cuma mulai dari {habit}, bukan nunggu semangat penuh.",
        "📌 Tarik napas, lalu sikat {habit}. Selesai sedikit pun tetap berarti.",
        "⚡ Buka momentum lewat {habit}. Begitu mulai, sisanya biasanya ikut jalan.",
        "🌈 Hari ini bisa tetap cakep kalau {habit} ikut kamu rapikan.",
        "🏁 {habit} cocok banget buat jadi start yang ringan tapi ngaruh.",
        "🪄 Sentuh {habit} sebentar, biar pikiranmu tidak terus kebebanan ingat itu.",
        "🌻 Ada progres manis yang bisa kamu ambil dari {habit} sekarang juga.",
        "📈 {habit} itu kecil kalau dicicil, berat kalau terus ditunda.",
        "🔥 Gas tipis-tipis di {habit}. +{xp} XP itu bonus yang enak.",
        "✨ Kalau bingung mau mulai dari mana, {habit} adalah jawaban yang cukup bagus.",
        "🎮 Anggap saja {habit} sebagai misi sampingan yang ternyata penting.",
        "🚶 Tidak usah lari, cukup jalan ke {habit} dulu dan biarkan progres terbentuk.",
        "🌟 {habit} bisa jadi bukti kalau kamu tetap gerak walau tidak sempurna.",
        "💡 Kemenangan kecil dari {habit} sering bikin beban hari terasa turun.",
        "🎯 Saat kamu beresin {habit}, kamu bukan cuma cari XP, tapi juga ritme.",
        "⚓ {habit} bisa jadi jangkar biar hari ini tidak berantakan.",
        "🧠 Pusingnya sering berkurang setelah {habit} mulai disentuh.",
        "🏆 Satu check di {habit} kadang lebih powerful dari motivasi panjang.",
        "🌊 Biar alurnya ngalir, mulai saja dari {habit} sekarang.",
        "🔥 Jangan kasih {habit} keburu basi di daftar. Beresin dikit dulu.",
        "✨ {habit} ini cocok buat nyalain mode produktifmu tanpa drama.",
        "📚 Kerjain {habit} sedikit, lalu lihat bagaimana mood fokusmu ikut naik.",
        "🪜 {habit} itu satu anak tangga. Kecil, tapi jelas bikin naik.",
        "🎨 Hari terasa lebih rapi kalau {habit} tidak terus nongkrong di daftar.",
        "💪 {habit} bukan buat sempurna, cuma buat maju sedikit lebih jauh.",
        "🧭 Balik ke jalur lewat {habit}. Tidak perlu spektakuler, cukup jadi.",
        "🌿 Bikin tenang dengan nyicil {habit} sekarang.",
        "🚀 Kalau {habit} kelar, kamu bakal berasa lebih ringan lanjut yang lain.",
        "🎯 Coba kasih 5 menit buat {habit}. Sering kali itu sudah cukup buat mulai.",
        "🔥 Sedikit progres di {habit} tetap lebih keren daripada nol besar.",
        "✨ Kamu tidak harus mood dulu buat beresin {habit}. Mulai saja dulu.",
        "🏁 {habit} bisa kamu ubah dari beban jadi kemenangan kecil hari ini.",
        "📌 Jangan tunggu versi paling rajin dari dirimu. Versi sekarang juga bisa kerjain {habit}.",
        "⚡ {habit} cocok dijadikan pemantik supaya energi produktifmu bangun lagi.",
        "🌟 Ada rasa puas kecil yang nunggu setelah {habit} disentuh.",
        "💎 {habit} kelihatan biasa, tapi efek rapihnya ke hari ini lumayan besar.",
        "🧩 Pecah kebuntuan dengan mulai dari {habit}.",
        "🎵 Cukup satu progres di {habit}, lalu biarkan ritme kerja ikut kebangun.",
        "🌈 Jangan biarkan {habit} cuma jadi dekorasi daftar misi.",
        "🪄 Sihir kecil hari ini mungkin cuma: buka {habit}, kerjakan sebentar, lanjut hidup.",
        "🔥 +{xp} XP enak, tapi rasa lega setelah {habit} selesai lebih enak lagi.",
        "💡 {habit} cocok buat kamu amankan dulu sebelum pikiran keburu ke mana-mana.",
        "🚶 Satu langkah ke {habit} tetap langkah. Dan itu sudah bagus.",
        "🏆 Biar streak tetap ganteng, bantu hari ini lewat {habit}.",
        "🌻 Kalau mau hadiah kecil untuk diri sendiri, mulai dari {habit}.",
        "📈 {habit} itu investasi kecil buat versi kamu yang nanti malam lebih tenang.",
        "🎯 Buka {habit}, cicil sebentar, lalu lihat dirimu mulai lebih enak bergerak.",
        "⚡ Kadang progres paling keren justru dimulai dari {habit} yang sederhana.",
        "✨ {habit} tidak minta sempurna, cuma minta disentuh sekarang.",
        "🔥 Kecil, singkat, tapi ngaruh. Itu vibes yang pas buat {habit}.",
        "🌿 Rapikan {habit} sedikit biar kepala juga ikut longgar.",
        "💪 Begitu {habit} mulai jalan, kamu biasanya susah berhenti di satu progres saja."
    )

    private val slotBodies = listOf(
        "🔥 Masih ada {count} misi yang bisa kamu amankan. Mulai dari {habit} dulu, yuk.",
        "✨ Hari ini belum tertutup. {habit} bisa jadi pembuka ritme yang enak.",
        "🚀 Ada {count} target yang masih hidup, dan {habit} kelihatan paling pas buat disentuh dulu.",
        "🌟 Kamu tidak harus menaklukkan semuanya sekarang. Cukup mulai dari {habit}.",
        "💪 {habit} bisa jadi pintu masuk buat ngerapihin {count} misi yang tersisa.",
        "🎯 Kalau bingung, pilih {habit} dulu. Sisanya sering jadi lebih gampang setelah itu.",
        "🧭 Arah produktifmu masih aman. Tinggal buktikan lewat {habit}.",
        "📌 Masih ada {count} hal yang menunggu, tapi satu langkah ke {habit} sudah sangat cukup.",
        "⚡ Curi satu kemenangan kecil lewat {habit}, lalu biarkan mood kerja ikut kebangun.",
        "🌈 Tidak harus full gas. {habit} dulu aja, lalu lihat alurnya bergerak.",
        "🏁 {habit} bisa jadi check pertama yang bikin daftar misi terasa lebih jinak.",
        "🪄 Sedikit sentuhan ke {habit} bisa bikin seluruh daftar terasa tidak semenakutkan itu.",
        "🌻 Hari ini masih bisa dibikin manis. Mulai dari {habit}.",
        "📈 Dari {count} misi yang tersisa, {habit} kelihatan paling cocok buat start.",
        "🎮 Anggap saja {habit} sebagai level pertama sebelum kamu ngerjain yang lain.",
        "🌿 Pelan saja. {habit} bisa jadi langkah kecil yang menyelamatkan ritme hari ini.",
        "💎 Masih ada ruang buat progres, dan {habit} layak kamu sentuh dulu.",
        "🔥 Kalau satu misi dulu, pilih {habit}. Itu sudah cukup buat jaga momentum.",
        "✨ {habit} bisa kamu bereskan buat ngasih sinyal ke otak: kita masih jalan.",
        "🚶 Dari {count} yang tersisa, satu langkah ke {habit} sudah termasuk progres yang bagus.",
        "🏆 Ada kemenangan kecil yang menunggu di {habit}. Kamu tinggal ambil saja.",
        "🎯 {habit} cocok dijadikan misi pembuka supaya beban yang lain ikut turun.",
        "⚓ Jangan lihat semuanya sekaligus. Fokus ke {habit}, habis itu baru lanjut.",
        "🌊 Ritme kerja kadang cuma butuh satu pemicu, dan {habit} pas buat itu.",
        "💡 Masih ada {others} lagi setelah {habit}, tapi tidak usah dipikir semua sekarang.",
        "📚 Sentuh {habit} dulu, baru putuskan perlu lanjut atau cukup satu progres hari ini.",
        "🧠 Yang penting hari ini tidak kosong, dan {habit} bisa bantu itu.",
        "🔥 Mulai dari {habit}, lalu biarkan daftarmu pelan-pelan kehilangan tekanan.",
        "🌈 Hari ini belum selesai, tapi kamu masih punya banyak ruang buat menang kecil.",
        "✨ {habit} bisa jadi cara paling simpel buat tetap merasa on track."
    )
}
