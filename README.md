# GENeral Chat

Android için **Jetpack Compose + Material 3** ile geliştirilmiş; uçtan uca şifreli bulut mesajlaşma, WebRTC sesli/görüntülü arama, çevrimdışı yerel ağ (LAN) sunucusu, yapay zekâ asistanı (GEN AI) ve gelişmiş ekran kilidi koruması sunan gizlilik odaklı iletişim uygulaması.

## Temel Özellikler
- **Gerçek Zamanlı Bulut Sohbet & E2EE:** Firebase Authentication (Google Sign-In / Credential Manager) + Cloud Firestore üzerinden gerçek zamanlı senkronizasyon; 1'e 1 sohbetlerde **ECDH (`secp256r1`) + AES-256-GCM** uçtan uca şifreleme ve parmak izi doğrulama.
- **WebRTC Sesli & Görüntülü Arama:** Firestore sinyalleme destekli DTLS-SRTP şifreli gerçek zamanlı görüşme.
- **Ücretsiz Yerel Ağ (LAN / Hotspot) Sunucusu (`LocalMeshServer`):** İnternet olmadan aynı Wi-Fi/Hotspot üzerindeki telefon, tablet ve tarayıcılardan (Chrome, Firefox, Safari) erişilebilen gömülü HTTP/JSON sohbet ve dosya köprüsü.
- **Sertleştirilmiş 4 Haneli PIN & İMHA Protokolü:** Açılışta, arka plana alınmada ve panik anında kilitleme; her 5 hatalı denemede artan bekleme cezası (`1 dk → 6 dk → 11 dk...`) ve unutulan şifre durumunda yerel + bulut hesabını tamamen temizleyen **İMHA** mekanizması.
- **Kamuflaj & Gizlilik:** 10 uygulama içi görünüm maskesi, Kağıt Ekran (Paper Mode), ebeveyn denetimi, küfür filtresi ve KVKK 6698 uyumlu JSON dışa aktarım + hesap silme.
- **GEN AI:** Çok turlu bellek, kod üretimi, çok dilli sözlük, sesli okuma (TTS) ve çevrimdışı yedek motor.

## GitHub Dağıtım & Yayın Koşulları (Release Checklist)
1. **Gizlilik ve Lisans Belgeleri:** Depo kökünde [LICENSE](LICENSE), [PRIVACY.md](PRIVACY.md) ve [SECURITY.md](SECURITY.md) dosyaları yer alır.
2. **Gizli Anahtarların Korunması:** `.gitignore` dosyası `*.jks`, `*.keystore`, `.env` ve derlenmiş APK/AAB çıktılarını hariç tutar. İmza anahtarlarını asla commit etmeyin; CI/yerel release derlemelerinde `KEYSTORE_PATH`, `STORE_PASSWORD` ve `KEY_PASSWORD` ortam değişkenlerini kullanın.
3. **Firebase Parmak İzleri:** GitHub Releases üzerinden dağıtılacak imzalı Release APK'da **Google ile Giriş** özelliğinin çalışması için release keystore'unuzun **SHA-1** ve **SHA-256** parmak izlerini Firebase Console'a ekleyin.
4. **APK Dağıtımı:** Derlenen imzalı APK dosyasını (ve isteğe bağlı `SHA256SUMS.txt` doğrulama özetini) GitHub **Releases** sekmesi üzerinden yayınlayın.

## Derleme ve Test
```bash
# Uygulamayı derlemek için:
./gradlew :app:assembleDebug

# Yerel birim ve Robolectric testlerini çalıştırmak için:
./gradlew :app:testDebugUnitTest
```
> Not: Release derlemesi (`assembleRelease`) öncesinde `KEYSTORE_PATH`, `STORE_PASSWORD` ve `KEY_PASSWORD` ortam değişkenlerini tanımlayın.
