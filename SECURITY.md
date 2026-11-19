# Güvenlik Politikası (Security Policy)

## Desteklenen Sürümler

| Sürüm | Durum |
| ----- | ----- |
| Güncel `main` / son GitHub Release APK | Destekleniyor |

## Güvenlik Mimarisi Özeti
- **Uçtan Uca Şifreleme (E2EE):** Bire bir bulut sohbetlerinde `ECDH (secp256r1)` anahtar değişimi ve `AES-256-GCM` şifrelemesi cihaz içinde uygulanır. Şifre çözme yalnızca katılımcı cihazlardaki özel anahtarlarla gerçekleşir.
- **Taşıma Güvenliği:** İnternet/bulut trafiğinde `network_security_config.xml` üzerinden genel ağ için düz metin HTTP kapatılmıştır (`cleartextTrafficPermitted="false"`). Yalnızca cihaz içi yerel LAN köprüsü için `localhost`/`127.0.0.1` istisnası bulunur.
- **Yedekleme Engeli:** `android:allowBackup="false"` ile yerel veritabanı, E2EE anahtarları ve kilit bilgileri Google Auto Backup kapsamı dışında tutulur.
- **Ekran Kilidi & Artan Bekleme Cezası:** 4 haneli PIN ve parolalar rastgele salt + SHA-256 ile saklanır. Her 5 hatalı denemede artan bekleme süresi (1 dk, 6 dk, 11 dk, +5 dk artış) uygulanır. Unutulan şifre durumunda yalnızca tam yerel + bulut sıfırlama yapan **İMHA** seçeneği bulunur.
- **Sıfır Güven Firestore Kuralları:** `firestore.rules` tüm kullanıcı, sohbet, mesaj, arama, konum ve dosya sinyalleme belgelerini doğrulanmış katılımcı UID eşleşmesiyle sınırlar.

## Güvenlik Açığı Bildirimi
Lütfen tespit ettiğiniz güvenlik açıklarını herkese açık Issue olarak **açmayın**.
- GitHub deposunda **Security → Advisories → Report a vulnerability** (Private Vulnerability Reporting) özelliğini kullanın veya
- Geliştirici kanalı [@GENeralYTB](https://youtube.com/@GENeralYTB) üzerinden iletişime geçin.

## İmzalama & Gizli Anahtar Güvenliği
- Yükleme anahtarları (`*.jks`, `*.keystore`), `.env` ve imza parolaları hiçbir zaman Git deposuna dahil edilmez; yalnızca GitHub Actions Secrets veya yerel güvenli ortam değişkenleri (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`) üzerinden sağlanır.
