package com.example.data

data class LanguageOption(
    val countryName: String,
    val languageName: String,
    val flag: String,
    val code: String
)

val defaultLanguages = listOf(
    LanguageOption("Türkiye", "Türkçe (TR)", "🇹🇷", "tr"),
    LanguageOption("United States", "English (US)", "🇺🇸", "en"),
    LanguageOption("Deutschland", "Deutsch (DE)", "🇩🇪", "de"),
    LanguageOption("United Kingdom", "English (UK)", "🇬🇧", "en-GB"),
    LanguageOption("France", "Français (FR)", "🇫🇷", "fr"),
    LanguageOption("España", "Español (ES)", "🇪🇸", "es"),
    LanguageOption("Japan", "日本語 (JA)", "🇯🇵", "ja"),
    LanguageOption("South Korea", "한국어 (KO)", "🇰🇷", "ko"),
    LanguageOption("Azerbaijan", "Azərbaycan (AZ)", "🇦🇿", "az"),
    LanguageOption("Italy", "Italiano (IT)", "🇮🇹", "it")
)

enum class MaskType(val title: String, val subtitle: String, val iconResName: String, val aliasClass: String) {
    WEATHER("Hava Durumu", "Canlı meteoroloji & tahminler", "ic_weather", "com.example.MainActivityWeather"),
    CALCULATOR("Hesap Makinesi", "Hızlı matematiksel işlemler", "ic_calc", "com.example.MainActivityCalculator"),
    NOTES("Not Defteri", "Kişisel yapılacaklar ve notlar", "ic_notes", "com.example.MainActivityNotes"),
    COMPASS("Pusula", "Manyetik yön ve koordinat", "ic_compass", "com.example.MainActivityCompass"),
    CALENDAR("Takvim", "Etkinlikler ve randevular", "ic_calendar", "com.example.MainActivityCalendar"),
    CLOCK("Saat / Alarm", "Dünya saati ve alarmlar", "ic_clock", "com.example.MainActivityClock"),
    STOPWATCH("Kronometre", "Hassas tur sayacı", "ic_stopwatch", "com.example.MainActivityStopwatch"),
    DICTIONARY("Sözlük", "Çok dilli sözlük", "ic_dictionary", "com.example.MainActivityDictionary"),
    QR_SCANNER("QR Okuyucu", "Kamera ile barkod tarama", "ic_qr", "com.example.MainActivityQr"),
    BATTERY("Pil Tasarrufu", "Enerji profili & optimizasyon", "ic_battery", "com.example.MainActivityBattery")
}

data class CustomAiDisguise(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val secretTrigger: String,
    val iconEmoji: String = "🛡️"
)

enum class PanicTrigger(val label: String, val description: String) {
    SHAKE("Telefonu sallayarak tetikle", "Hızlı bir bilek hareketiyle anında kamuflaja geç"),
    TRIPLE_TAP("Parmak izi okuyucu kısmına 3 kez dokunarak tetikle", "Saniyeler içinde devreye sokulabilen sessiz kalkan")
}

data class PanicConfig(
    val isEnabled: Boolean = true,
    val trigger: PanicTrigger = PanicTrigger.SHAKE,
    val targetMask: MaskType = MaskType.CALCULATOR,
    val dontShowAgain: Boolean = false,
    val wipeHistoryOnPanic: Boolean = true
)

data class PollOption(
    val id: Int,
    val text: String,
    var votes: Int
)

data class ChannelPoll(
    val id: String,
    val question: String,
    val options: List<PollOption>,
    var userVotedOptionId: Int? = null
)
