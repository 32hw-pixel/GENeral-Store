# Gizlilik Politikası ve KVKK 6698 Aydınlatma Metni

**Uygulama:** GENeral Chat  
**Geliştirici / Veri Sorumlusu:** GENeralYTB (Bireysel Geliştirici)  
**Dağıtım:** GitHub Releases  

## 1. Toplanan ve İşlenen Veriler
- **Hesap ve Kimlik (İsteğe Bağlı):** Google ile giriş yapıldığında Firebase Authentication üzerinden UID, görünen ad, e-posta ve kullanıcının belirlediği `@kullanıcı` kimliği işlenir. Hesapsız (misafir) modda GEN AI ve cihaz içi `Siz` notları bulut hesabı olmadan kullanılabilir.
- **Mesajlaşma ve E2EE:** Bire bir bulut sohbetlerinde metin mesajları cihaz içinde ECDH P-256 + AES-256-GCM ile şifrelenerek (`E2EE:v1:...`) Firestore'a yazılır; sunucuda okunabilir düz metin tutulmaz. Grup sohbetleri ve henüz anahtar eşleşmesi yapılmamış oturumlar TLS koruması altındadır.
- **Sesli & Görüntülü Aramalar:** WebRTC (DTLS-SRTP) ile peer-to-peer aktarılır; yalnızca çağrı sinyalleme (`calls/{callId}`) Firestore üzerinden koordine edilir.
- **Yerel Ağ (Wi-Fi/Hotspot LAN) Sunucusu:** Kullanıcı manuel başlatmadıkça port açmaz veya NSD yayını yapmaz; bağlantılar yalnızca özel yerel ağ IP'leriyle sınırlıdır.

## 2. İzin Politikası
- Depolama için geniş izinler (`READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`) **talep edilmez**; medya seçimi Android Photo Picker ile yapılır.
- Kamera, Mikrofon, Konum ve Bildirim izinleri yalnızca ilgili özellik kullanıldığında çalışma zamanında istenir.
- Üçüncü taraf reklam SDK'sı veya gizli arka plan telemetrisi bulunmaz.

## 3. Kullanıcı Hakları ve Veri Silme (KVKK Md. 11)
Uygulama içindeki **Ayarlar → Gizlilik, KVKK ve Uyumluluk** menüsünden:
- Tüm yerel ve bulut verilerinizi **JSON** olarak dışa aktarabilir,
- Görünen adınızı düzeltebilir,
- Cihaz verilerinizi veya Google bulut hesabınızı + ilişkili kayıtlarınızı kalıcı olarak silebilirsiniz.
- Ayrıca kilit ekranındaki **İMHA** seçeneği tüm yerel ve bulut verilerini kalıcı olarak temizler.

## 4. İletişim
- Uygulama içi Elektronik KVKK Md. 11 Başvuru Portalı
- Geliştirici YouTube Kanalı: [@GENeralYTB](https://youtube.com/@GENeralYTB)
- GitHub Deposu üzerinden Issue / Security Advisory bildirimleri
