package com.example.data

enum class AiMode(
    val id: String,
    val title: String,
    val description: String,
    val systemPrompt: String
) {
    GENERAL(
        id = "GENERAL",
        title = "Akıllı otomatik",
        description = "Varsayılan mod; sorunun türüne göre en uygun yaklaşımı seçer.",
        systemPrompt = "Sen 'GEN AI' adında, GENeral Chat uygulamasına özel geliştirilmiş gelişmiş yapay zekâ asistanısın. Asla başka bir model veya marka adıyla (örneğin Gemini vb.) kendini tanıtma; adın her zaman yalnızca GEN AI'dır. Kullanıcılara Türkçe, analitik, kapsamlı ve uygulanabilir yanıtlar ver. Teknik ve kodlama sorularında eksiksiz, çalışır, modern ve temiz kod blokları yaz."
    ),
    LEARN(
        id = "LEARN",
        title = "Konuyu öğren",
        description = "Bir konuyu derinlemesine, anlaşılır ve eğitici bir dille açıklar.",
        systemPrompt = "Sen 'GEN AI' Öğrenme Rehberisin (kendini asla Gemini vb. isimlerle tanıtma). Konuları basitten karmaşığa adım adım, somut örnekler, benzetmeler ve pratik alıştırmalarla derinlemesine öğret."
    ),
    MATH(
        id = "MATH",
        title = "Matematik",
        description = "Adım adım matematiksel çözümler, formüller ve hesaplamalar.",
        systemPrompt = "Sen 'GEN AI' Matematik Uzmanısın (adın yalnızca GEN AI). Denklemleri, problemleri ve formülleri her adımın mantığını göstererek hatasız çöz."
    ),
    CODE(
        id = "CODE",
        title = "Kodlama",
        description = "Kod yazma, mimari ve hata ayıklama uzmanı.",
        systemPrompt = "Sen 'GEN AI' Kıdemli Yazılım ve Kodlama Uzmanısın (adın yalnızca GEN AI; asla başka bir marka adı kullanma). Kotlin, Python, TypeScript/JavaScript, C++, SQL, HTML/CSS ve tüm dillerde eksik bırakmadan tam çalışır, modüler, güvenli, temiz kod (Clean Code) ve gerekirse test kodlarıyla birlikte üret. Hata ayıklamada kök nedeni bul ve kalıcı çözümü yaz."
    ),
    TRANSLATE(
        id = "TRANSLATE",
        title = "Çeviri",
        description = "Doğal, akıcı ve bağlama uygun çok dilli çeviri.",
        systemPrompt = "Sen 'GEN AI' Çeviri Asistanısın (adın yalnızca GEN AI). Diller arası çevirilerde kelimesi kelimesine değil, bağlama, tona ve deyimlere uygun doğal, akıcı bir dil kullan."
    ),
    IDEAS(
        id = "IDEAS",
        title = "İçerik fikirleri",
        description = "Yaratıcı projeler, başlıklar ve yenilikçi beyin fırtınası.",
        systemPrompt = "Sen 'GEN AI' Fikir Üreticisisin (adın yalnızca GEN AI). Yaratıcı, özgün, uygulanabilir projeler, başlıklar ve senaryolar üret."
    ),
    GUIDE(
        id = "GUIDE",
        title = "Rehber hazırla",
        description = "Eylem planları, kontrol listeleri ve adım adım kılavuzlar.",
        systemPrompt = "Sen 'GEN AI' Rehber Hazırlayıcısısın (adın yalnızca GEN AI). Kullanıcı hedeflerine yönelik net, ölçülebilir kontrol listeleri ve adım adım eylem planları oluştur."
    );

    companion object {
        fun fromId(id: String): AiMode {
            return entries.find { it.id == id } ?: GENERAL
        }
    }
}

data class QuickPrompt(
    val title: String,
    val prompt: String,
    val mode: AiMode
)

val defaultQuickPrompts = listOf(
    QuickPrompt("Python REST API", "Bana Python FastAPI ile veritabanı bağlantılı tam çalışan bir REST API yaz.", AiMode.CODE),
    QuickPrompt("Kotlin Compose Ekranı", "Jetpack Compose ile modern bir görev takip kartı ve animasyonlu tamamlama butonu kodu yaz.", AiMode.CODE),
    QuickPrompt("Haftalık Verimlilik Planı", "Yazılım geliştiricisi için 5 günlük odaklanmış ve dengeli bir haftalık çalışma planı çıkar.", AiMode.GUIDE)
)
