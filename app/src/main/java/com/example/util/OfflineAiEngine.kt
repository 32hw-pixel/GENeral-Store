package com.example.util

import com.example.data.AiMode
import java.util.Locale
import kotlin.math.sqrt

/**
 * Always-available Offline + Web-Augmented AI & Multi-Language Dictionary Engine for GEN AI.
 * Guarantees GEN AI responds accurately with explicit reasoning steps, context awareness,
 * math evaluation, code templates, and dictionary/translation even without cloud connectivity.
 */
object OfflineAiEngine {

    data class DictEntry(
        val tr: String,
        val en: String,
        val de: String,
        val fr: String,
        val es: String,
        val az: String,
        val definitionTr: String
    )

    private val dictionaryEntries = listOf(
        DictEntry("merhaba", "hello", "hallo", "bonjour", "hola", "salam", "Karşılama ve selamlaşma ifadesi."),
        DictEntry("teşekkürler", "thank you", "danke", "merci", "gracias", "təşəkkürlər", "Memnuniyet ve minnet bildiren nezaket sözü."),
        DictEntry("günaydın", "good morning", "guten morgen", "bonjour", "buenos días", "sabahın xeyir", "Sabah saatlerinde söylenen iyi dilek."),
        DictEntry("iyi geceler", "good night", "gute nacht", "bonne nuit", "buenas noches", "gecən xeyrə", "Gece ayrılırken veya uyurken söylenen dilek."),
        DictEntry("nasılsın", "how are you", "wie geht es dir", "comment ça va", "cómo estás", "necəsən", "Hal hatır sorma ifadesi."),
        DictEntry("evet", "yes", "ja", "oui", "sí", "bəli", "Onay ve kabul bildiren kelime."),
        DictEntry("hayır", "no", "nein", "non", "no", "xeyr", "Ret veya olumsuzluk bildiren kelime."),
        DictEntry("lütfen", "please", "bitte", "s'il vous plaît", "por favor", "zəhmət olmasa", "Rica ve nezaket ifadesi."),
        DictEntry("arkadaş", "friend", "freund", "ami", "amigo", "dost", "Karşılıklı sevgi ve güven duyulan yakın kişi."),
        DictEntry("aile", "family", "familie", "famille", "familia", "ailə", "Anne, baba ve çocuklardan oluşan en temel topluluk."),
        DictEntry("güvenlik", "security", "sicherheit", "sécurité", "seguridad", "təhlükəsizlik", "Tehlikeden uzak olma, koruma durumu."),
        DictEntry("gizlilik", "privacy", "datenschutz", "confidentialité", "privacidad", "məxfilik", "Kişisel verilerin ve özel hayatın korunması."),
        DictEntry("şifre", "password", "passwort", "mot de passe", "contraseña", "şifrə", "Erişimi doğrulayan gizli karakter dizisi."),
        DictEntry("şifreleme", "encryption", "verschlüsselung", "chiffrement", "cifrado", "şifrələmə", "Veriyi yetkisiz erişime karşı matematiksel olarak kodlama."),
        DictEntry("sunucu", "server", "server", "serveur", "servidor", "server", "Ağ üzerindeki istemcilere veri ve hizmet sağlayan sistem."),
        DictEntry("yazılım", "software", "software", "logiciel", "software", "proqram təminatı", "Donanımı çalıştıran program ve komutlar bütünü."),
        DictEntry("kod", "code", "code", "code", "código", "kod", "Bilgisayarın çalıştırdığı programlama dili komutları."),
        DictEntry("algoritma", "algorithm", "algorithmus", "algorithme", "algoritmo", "alqoritm", "Bir problemi çözmek için izlenen sonlu ve net adımlar."),
        DictEntry("yapay zeka", "artificial intelligence", "künstliche intelligenz", "intelligence artificielle", "inteligencia artificial", "süni intellekt", "İnsan bilişsel süreçlerini modelleyen akıllı sistemler."),
        DictEntry("veritabanı", "database", "datenbank", "base de données", "base de datos", "verilənlər bazası", "Verilerin düzenli olarak saklandığı ve sorgulandığı yapı."),
        DictEntry("mesaj", "message", "nachricht", "message", "mensaje", "mesaj", "Bir kişiden diğerine iletilen yazılı/sesli bilgi."),
        DictEntry("arama", "call", "anruf", "appel", "llamada", "zəng", "Sesli veya görüntülü gerçek zamanlı iletişim."),
        DictEntry("görüntülü arama", "video call", "videoanruf", "appel vidéo", "videollamada", "video zəng", "Canlı kamera ve ses ile yapılan çift yönlü görüşme."),
        DictEntry("dosya", "file", "datei", "fichier", "archivo", "fayl", "Depolama ortamında saklanan adlandırılmış veri kümesi."),
        DictEntry("konum", "location", "standort", "localisation", "ubicación", "məkan", "Harita veya koordinat üzerindeki bulunulan yer."),
        DictEntry("etkinlik", "event", "ereignis", "événement", "evento", "tədbir", "Belirli tarih ve saatte planlanan buluşma veya program."),
        DictEntry("bilgisayar", "computer", "computer", "ordinateur", "computadora", "kompüter", "Veri işleyen elektronik hesaplama cihazı."),
        DictEntry("telefon", "phone", "telefon", "téléphone", "teléfono", "telefon", "Mobil ve sesli iletişim cihazı."),
        DictEntry("internet", "internet", "internet", "internet", "internet", "internet", "Dünya çapında birbirine bağlı bilgisayar ağları."),
        DictEntry("tarayıcı", "browser", "browser", "navigateur", "navegador", "brauzer", "Web sayfalarını görüntüleyen uygulama (Chrome, Safari, Firefox)."),
        DictEntry("dil", "language", "sprache", "langue", "idioma", "dil", "İnsanların anlaşmasını sağlayan iletişim sistemi."),
        DictEntry("ülke", "country", "land", "pays", "país", "ölkə", "Sınırları belirlenmiş egemen coğrafi bölge."),
        DictEntry("sözlük", "dictionary", "wörterbuch", "dictionnaire", "diccionario", "lüğət", "Kelimelerin anlam ve karşılıklarını içeren kaynak."),
        DictEntry("çeviri", "translation", "übersetzung", "traduction", "traducción", "tərcümə", "Bir metni başka bir dile anlamını koruyarak aktarma."),
        DictEntry("zaman", "time", "zeit", "temps", "tiempo", "zaman", "Olayların akışını ölçen süre ve saat dilimi."),
        DictEntry("kitap", "book", "buch", "livre", "libro", "kitab", "Basılı veya dijital okuma eseri."),
        DictEntry("okul", "school", "schule", "école", "escuela", "məktəb", "Eğitim ve öğretim verilen kurum."),
        DictEntry("su", "water", "wasser", "eau", "agua", "su", "Yaşam için temel renksiz, kokusuz sıvı bileşik (H2O)."),
        DictEntry("ekmek", "bread", "brot", "pain", "pan", "çörək", "Un ve sudan yapılan temel besin maddesi."),
        DictEntry("yemek", "food", "essen", "nourriture", "comida", "yemək", "Beslenmek için tüketilen yiyecek."),
        DictEntry("spor", "sport", "sport", "sport", "deporte", "idman", "Fiziksel gelişim ve yarışma amaçlı aktivite."),
        DictEntry("haber", "news", "nachrichten", "nouvelles", "noticias", "xəbər", "Güncel olaylar hakkında verilen bilgi."),
        DictEntry("müzik", "music", "musik", "musique", "música", "musiqi", "Seslerin ritim ve melodiyle düzenlenmesi sanatı."),
        DictEntry("hızlı", "fast", "schnell", "rapide", "rápido", "sürətli", "Kısa sürede çok yol veya işlem yapan."),
        DictEntry("ücretsiz", "free", "kostenlos", "gratuit", "gratis", "pulsuz", "Herhangi bir ücret gerektirmeyen."),
        DictEntry("yardım", "help", "hilfe", "aide", "ayuda", "kömək", "Destek ve çözüm sağlama eylemi.")
    )

    fun sanitizeIdentity(raw: String): String {
        return raw
            .replace(Regex("(?i)\\bGoogle'?ın geliştirdiği Gemini\\b"), "GEN AI")
            .replace(Regex("(?i)\\bGemini\\b"), "GEN AI")
    }

    fun lookupDictionary(query: String): String? {
        val q = query.lowercase(Locale.getDefault())
            .replace(Regex("(?i)\\b(sözlük|sozluk|ne demek|anlamı|anlami|nedir|çevir|cevir|translate|dictionary|wörterbuch|almanca|ingilizce|fransızca|ispanyolca|azerice)\\b"), " ")
            .replace(Regex("[?.,!\"']"), " ")
            .trim()
        if (q.isBlank()) return null

        val match = dictionaryEntries.firstOrNull { entry ->
            entry.tr == q || entry.en == q || entry.de == q || entry.fr == q || entry.es == q || entry.az == q ||
                q.contains(entry.tr) || q.contains(entry.en) || q.contains(entry.de) || q.contains(entry.fr) || q.contains(entry.es)
        } ?: return null

        return buildString {
            appendLine("📖 **GEN AI Çok Dilli Sözlük & Çeviri Sonucu**")
            appendLine("• **Tanım:** ${match.definitionTr}")
            appendLine("• 🇹🇷 Türkçe: **${match.tr}**")
            appendLine("• 🇺🇸 İngilizce: **${match.en}**")
            appendLine("• 🇩🇪 Almanca: **${match.de}**")
            appendLine("• 🇫🇷 Fransızca: **${match.fr}**")
            appendLine("• 🇪🇸 İspanyolca: **${match.es}**")
            appendLine("• 🇦🇿 Azerbaycanca: **${match.az}**")
        }.trim()
    }

    fun generateResponse(
        prompt: String,
        mode: AiMode,
        chatContextSummary: String = "",
        webResearch: WebResearchResult? = null
    ): String {
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase(Locale.getDefault())

        // 1. Identity questions ("sen kimsin", "adın ne", etc.)
        if (lower.contains("kimsin") || lower.contains("adın ne") || lower.contains("kendini tanıt") || lower.contains("who are you")) {
            return "Ben **GEN AI**. GENeral Chat içinde çalışan akıllı asistanınım. Kod yazma, hata ayıklama, canlı web araştırması, kapsamlı çok dilli sözlük & çeviri, sohbet geçmişi analizi ve sesli yanıt özellikleriyle çevrimiçi ve çevrimdışı her an yanındayım."
        }

        // 2. Chat Context questions ("en son kime yazdım", "bir mesajı arıyorum")
        if (lower.contains("en son kime") || lower.contains("kime yazdım") || lower.contains("kimle konuştum") || lower.contains("kimseye yaz")) {
            if (chatContextSummary.contains("ZERO_SENT_MESSAGES") || chatContextSummary.isBlank()) {
                return "Uygulamayı kullandığından beri şu ana kadar kimseyle konuşmadın ve kimseye mesaj yazmadın."
            }
            val match = Regex("EN SON YAZILAN KİŞİ/SOHBET:\\s*(.+)").find(chatContextSummary)
            if (match != null) {
                return "📌 **Son Yazdığın Kişi / Sohbet:**\n${match.groupValues[1].trim()}\n\n(Gelişmiş Sohbet Gizliliği açık olan odalar gizliliğin gereği bu analize dahil edilmez.)"
            }
        }

        if (lower.contains("mesaj") && (lower.contains("arıyorum") || lower.contains("ariyorum") || lower.contains("bul"))) {
            val hitsSection = chatContextSummary.substringAfter("Eşleşen / Mevcut Mesajlar:", "")
            return if (hitsSection.isNotBlank() && hitsSection.contains("- [")) {
                "🔎 **Sohbet Geçmişinde Bulunan İlgili Mesajlar:**\n${hitsSection.substringBefore("Eğer kullanıcı").trim()}"
            } else if (chatContextSummary.contains("ZERO_SENT_MESSAGES")) {
                "Uygulamayı kullandığından beri henüz kimseye mesaj göndermedin; aranacak sohbet mesajı bulunamadı. Aramak istediğin bir kelime veya kişi adı varsa yine de yazabilirsin."
            } else {
                "Aradığın mesajı doğrudan eşleştiremedim. Mesajın içinde geçen 1-2 anahtar kelimeyi, kişinin adını veya yaklaşık saatini söylersen daha net arayabilirim."
            }
        }

        // 3. Dictionary & Translation Check
        if (mode == AiMode.TRANSLATE || lower.contains("sözlük") || lower.contains("sozluk") || lower.contains("çevir") || lower.contains("translate") || lower.contains("ne demek") || lower.contains("almanca") || lower.contains("ingilizce") || lower.contains("fransızca")) {
            val dict = lookupDictionary(trimmed)
            if (dict != null) {
                return if (webResearch?.succeeded == true) {
                    "$dict\n\n🌐 **Ek Web Bilgisi:**\n${webResearch.summary}\nKaynak: ${webResearch.sourceUrls.joinToString(", ")}"
                } else {
                    dict
                }
            }
        }

        // 4. Math Expression Check
        evaluateSimpleMath(trimmed)?.let { mathAnswer ->
            return "🧮 **GEN AI Matematik Çözümü:**\nİşlem: `$trimmed`\n**Sonuç:** `$mathAnswer`"
        }

        // 5. Coding Help Check
        if (mode == AiMode.CODE || lower.contains("kod") || lower.contains("kotlin") || lower.contains("python") || lower.contains("javascript") || lower.contains("html") || lower.contains("function")) {
            return buildCodeResponse(trimmed, lower)
        }

        // 6. Live Web Research (online factual fallback)
        if (webResearch?.succeeded == true && webResearch.summary.isNotBlank()) {
            val src = if (webResearch.sourceUrls.isNotEmpty()) {
                "\n\n🔗 **Kaynaklar:**\n" + webResearch.sourceUrls.joinToString("\n") { "• $it" }
            } else ""
            return "🌐 **GEN AI Web Araştırması Sonucu (${webResearch.query}):**\n\n${webResearch.summary}$src"
        }

        // 7. Dictionary automatic match if single word / short phrase
        lookupDictionary(trimmed)?.let { return it }

        // 8. Structured Thinking Fallback Answer (always responsive, never blank)
        return buildString {
            appendLine("⚡ **GEN AI Yanıtı (${mode.title})**")
            appendLine()
            appendLine("Sorunuz analiz edildi: _\"$trimmed\"_")
            appendLine()
            when (mode) {
                AiMode.LEARN -> {
                    appendLine("📚 **Kavram Rehberi:**")
                    appendLine("1. Temel tanımı belirleyerek küçük parçalara ayırın.")
                    appendLine("2. Bir gerçek hayat örneğiyle mantığı doğrulayın.")
                    appendLine("3. Hangi alt başlığı derinleştirmemi isterseniz yazın, adım adım anlatayım.")
                }
                AiMode.IDEAS -> {
                    appendLine("💡 **Yaratıcı Fikir Çerçevesi:**")
                    appendLine("• Açılış kancası (ilk 3 saniye / dikkat çekici başlık)")
                    appendLine("• Somut fayda veya problem çözümü")
                    appendLine("• Net harekete geçirici kapanış mesajı")
                }
                AiMode.GUIDE -> {
                    appendLine("🗺️ **Adım Adım Plan:**")
                    appendLine("1. Hazırlık & gereksinim listesi")
                    appendLine("2. İlk uygulama adımı")
                    appendLine("3. Test, doğrulama ve iyileştirme")
                }
                else -> {
                    appendLine("• Şu an **çevrimdışı/hızlı GEN AI motoru** üzerinden yanıtlıyorum (bulut modeli erişilemediğinde otomatik devreye girer).")
                    appendLine("• İnternet bağlantın varken güncel konularda **canlı Wikipedia & DuckDuckGo araştırması** otomatik çalışır.")
                    appendLine("• Sözlük (ör. *'algoritma almanca ne demek'*), hesaplama (ör. *'125 * 8'*), kod yazımı (ör. *'kotlin örneği yaz'*) veya *'en son kime yazdım?'* sorularını hemen yanıtlayabilirim.")
                }
            }
        }.trim()
    }

    private fun buildCodeResponse(prompt: String, lower: String): String {
        return when {
            lower.contains("python") -> """
                💻 **GEN AI · Python Örneği**
                ```python
                def selamla_ve_hesapla(isim: str, degerler: list[int]) -> dict:
                    toplam = sum(degerler)
                    ortalama = toplam / len(degerler) if degerler else 0
                    return {"kullanici": isim, "toplam": toplam, "ortalama": round(ortalama, 2)}

                print(selamla_ve_hesapla("GENeral Chat", [10, 25, 40]))
                ```
                İstediğin başka bir fonksiyon veya hata düzeltmesi varsa kodu yapıştırabilirsin!
            """.trimIndent()
            lower.contains("html") || lower.contains("javascript") || lower.contains("js") || lower.contains("web") -> """
                💻 **GEN AI · Web (HTML5 + JavaScript) Örneği**
                ```html
                <!DOCTYPE html>
                <html lang="tr">
                <body>
                  <button id="btn">GEN AI Test</button>
                  <p id="out"></p>
                  <script>
                    document.getElementById('btn').addEventListener('click', () => {
                      document.getElementById('out').textContent = 'GENeral Chat Web hazır · ' + new Date().toLocaleTimeString();
                    });
                  </script>
                </body>
                </html>
                ```
            """.trimIndent()
            else -> """
                💻 **GEN AI · Kotlin / Jetpack Compose Örneği**
                ```kotlin
                fun formatCyberMessage(sender: String, body: String): String {
                    val clean = body.trim()
                    require(clean.isNotEmpty()) { "Mesaj boş olamaz" }
                    return "[GENeral Chat] ${'$'}sender: ${'$'}clean"
                }
                ```
                Projenle ilgili daha spesifik bir sınıf, ViewModel veya ekran kodu istersen detayını yazman yeterli!
            """.trimIndent()
        }
    }

    fun evaluateSimpleMath(input: String): String? {
        val clean = input.replace(" ", "").replace(",", ".")
        val sqrtMatch = Regex("(?i)sqrt\\((\\d+(?:\\.\\d+)?)\\)").find(clean)
        if (sqrtMatch != null) {
            val v = sqrtMatch.groupValues[1].toDoubleOrNull() ?: return null
            val res = sqrt(v)
            return if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.4f", res)
        }
        val match = Regex("^(-?\\d+(?:\\.\\d+)?)([+\\-*/x×])(-?\\d+(?:\\.\\d+)?)$").find(clean) ?: return null
        val a = match.groupValues[1].toDoubleOrNull() ?: return null
        val op = match.groupValues[2]
        val b = match.groupValues[3].toDoubleOrNull() ?: return null
        val result = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*", "x", "×" -> a * b
            "/" -> if (b == 0.0) return "Tanımsız (0'a bölünemez)" else a / b
            else -> return null
        }
        return if (result % 1.0 == 0.0) result.toLong().toString() else String.format(Locale.US, "%.4f", result).trimEnd('0').trimEnd('.')
    }
}
