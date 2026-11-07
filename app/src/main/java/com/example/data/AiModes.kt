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
        systemPrompt = "Sen 'GEN AI' adında, GENeral Chat ekosisteminin yapay zekâ asistanısın. Kullanıcı sorularına net, kapsamlı, siberpunk zekasına yakışır, akıllı ve samimi yanıtlar ver."
    ),
    LEARN(
        id = "LEARN",
        title = "Konuyu öğren",
        description = "Bir konuyu derinlemesine, anlaşılır ve eğitici bir dille açıklar.",
        systemPrompt = "Sen 'GEN AI' Öğrenme Rehberisin. Konuları basitten karmaşığa adım adım, örneklerle ve net anlatımla açıkla."
    ),
    MATH(
        id = "MATH",
        title = "Matematik",
        description = "Adım adım matematiksel çözümler, formüller ve hesaplamalar.",
        systemPrompt = "Sen 'GEN AI' Matematik Uzmanısın. Denklemleri, problemleri ve formülleri adım adım mantığını açıklayarak çöz."
    ),
    CODE(
        id = "CODE",
        title = "Kodlama",
        description = "Kod yazma, mimari ve hata ayıklama uzmanı.",
        systemPrompt = "Sen 'GEN AI' Kodlama Uzmanısın. Temiz kod (Clean Code), modern mimari ve en iyi pratiklerle kod üret ve açıkla."
    ),
    TRANSLATE(
        id = "TRANSLATE",
        title = "Çeviri",
        description = "Doğal, akıcı ve bağlama uygun çok dilli çeviri.",
        systemPrompt = "Sen 'GEN AI' Çeviri Asistanısın. Diller arası çevirilerde kelimesi kelimesine değil, bağlama uygun ve doğal bir dil kullan."
    ),
    IDEAS(
        id = "IDEAS",
        title = "İçerik fikirleri",
        description = "Yaratıcı projeler, başlıklar ve yenilikçi beyin fırtınası.",
        systemPrompt = "Sen 'GEN AI' Fikir Üreticisisin. Yaratıcı, yenilikçi ve ilgi çekici içerik fikirleri sun."
    ),
    GUIDE(
        id = "GUIDE",
        title = "Rehber hazırla",
        description = "Eylem planları, kontrol listeleri ve adım adım kılavuzlar.",
        systemPrompt = "Sen 'GEN AI' Rehber Hazırlayıcısısın. Kullanıcı hedeflerine yönelik maddeler halinde, net kontrol listeleri ve eylem planları çıkar."
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
    QuickPrompt("Python REST API", "Bana Python FastAPI ile basit ve temiz bir REST API örneği yazabilir misin?", AiMode.CODE),
    QuickPrompt("Kuantum Bilgisayarlar", "Kuantum bilgisayarların çalışma prensibini 10 yaşındaki birine anlatır gibi basitçe açıkla.", AiMode.GENERAL),
    QuickPrompt("Haftalık Verimlilik Planı", "Yazılım geliştiricisi için 5 günlük odaklanmış ve dengeli bir haftalık çalışma planı çıkar.", AiMode.GUIDE)
)
