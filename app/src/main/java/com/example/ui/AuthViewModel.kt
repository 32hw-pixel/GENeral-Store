package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuthRepository
import com.example.data.CloudHandleProfile
import com.example.data.FirestoreChatRepository
import com.example.data.LanguageOption
import com.example.data.MaskType
import com.example.data.PanicConfig
import com.example.data.SecurityUtils
import com.example.data.UserEntity
import com.example.data.defaultLanguages
import com.example.util.GoogleAuthHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _snackbarChannel = Channel<String>(Channel.BUFFERED)
    val snackbarEvents = _snackbarChannel.receiveAsFlow()

    // Roadmap Features State
    val isPaperMode = MutableStateFlow(false)
    val appThemeMode = MutableStateFlow(com.example.ui.theme.AppThemeMode.CYBER_DARK)
    val panicConfig = MutableStateFlow(PanicConfig())
    val selectedLanguage = MutableStateFlow(defaultLanguages[0])
    val appLockPasscode = MutableStateFlow<String?>(null)
    private val _hasAppLock = MutableStateFlow(false)
    val hasAppLock = _hasAppLock.asStateFlow()

    private val _isPinLockMode = MutableStateFlow(true)
    val isPinLockMode = _isPinLockMode.asStateFlow()

    private val _lockOnBackground = MutableStateFlow(true)
    val lockOnBackground = _lockOnBackground.asStateFlow()

    private val _lockOnPanic = MutableStateFlow(true)
    val lockOnPanic = _lockOnPanic.asStateFlow()

    val selectedMainTab = MutableStateFlow(0)

    @Volatile
    var suppressNextBackgroundLock: Boolean = false

    fun suppressNextBackgroundLockOnce() {
        suppressNextBackgroundLock = true
    }

    fun loadAppLockState(context: Context) {
        _hasAppLock.value = SecurityUtils.hasAppLockPasscode(context)
        _isPinLockMode.value = SecurityUtils.isAppLockPinMode(context)
        _lockOnBackground.value = SecurityUtils.isLockOnBackgroundEnabled(context)
        _lockOnPanic.value = SecurityUtils.isLockOnPanicEnabled(context)
    }

    fun setAppLockPasscode(
        context: Context,
        passcode: String,
        isPinMode: Boolean = passcode.trim().let { it.length == 4 && it.all { ch -> ch.isDigit() } }
    ) {
        val clean = passcode.trim()
        if (isPinMode && (clean.length != 4 || !clean.all { it.isDigit() })) {
            emitSnackbar("Ekran kilidi PIN kodu tam 4 rakamdan oluşmalıdır.")
            return
        }
        if (clean.length < 4) {
            emitSnackbar("Uygulama kilidi şifresi en az 4 karakter olmalıdır.")
            return
        }
        SecurityUtils.saveAppLockPasscode(context, clean, isPinMode = isPinMode)
        appLockPasscode.value = null
        _hasAppLock.value = true
        _isPinLockMode.value = isPinMode
        emitSnackbar(
            if (isPinMode) "4 haneli Ekran Kilidi PIN etkinleştirildi."
            else "Uygulama kilidi güvenli hash ile kaydedildi."
        )
    }

    fun setLockOnBackground(context: Context, enabled: Boolean) {
        _lockOnBackground.value = enabled
        SecurityUtils.setLockOnBackgroundEnabled(context, enabled)
        emitSnackbar(
            if (enabled) "Arka plana alınınca otomatik ekran kilidi açıldı."
            else "Arka planda otomatik kilitleme kapatıldı."
        )
    }

    fun setLockOnPanic(context: Context, enabled: Boolean) {
        _lockOnPanic.value = enabled
        SecurityUtils.setLockOnPanicEnabled(context, enabled)
        emitSnackbar(
            if (enabled) "Panik butonunda ekran kilidi PIN aktif."
            else "Panik butonunda PIN kilidi kapatıldı (kamuflaj maskesi açılır)."
        )
    }

    fun lockScreenIfConfigured(context: Context): Boolean {
        val active = SecurityUtils.hasAppLockPasscode(context) || !appLockPasscode.value.isNullOrBlank()
        if (active) {
            _hasAppLock.value = true
            _currentScreen.value = Screen.AppLockVerify
        }
        return active
    }

    fun verifyAppLockPasscode(context: Context, input: String): Boolean {
        return SecurityUtils.verifyAppLockPasscode(context, input) ||
            (!appLockPasscode.value.isNullOrBlank() && input == appLockPasscode.value)
    }

    fun clearAppLock(context: Context) {
        SecurityUtils.clearAppLockPasscode(context)
        appLockPasscode.value = null
        _hasAppLock.value = false
        _isPinLockMode.value = true
        emitSnackbar("Uygulama kilidi kaldırıldı.")
    }

    private val _fontScaleFactor = MutableStateFlow(1.0f)
    val fontScaleFactor = _fontScaleFactor.asStateFlow()

    private val _zoomEnabled = MutableStateFlow(false)
    val zoomEnabled = _zoomEnabled.asStateFlow()

    private val _zoomScaleFactor = MutableStateFlow(1.18f)
    val zoomScaleFactor = _zoomScaleFactor.asStateFlow()

    fun loadAppearancePreferences(context: Context) {
        val prefs = context.getSharedPreferences("general_chat_appearance", Context.MODE_PRIVATE)
        _fontScaleFactor.value = prefs.getFloat("font_scale_factor", 1.0f).coerceIn(0.85f, 1.45f)
        _zoomEnabled.value = prefs.getBoolean("zoom_enabled", false)
        _zoomScaleFactor.value = prefs.getFloat("zoom_scale_factor", 1.18f).coerceIn(1.05f, 1.40f)
        val savedTheme = prefs.getString("app_theme_mode", null)
        if (!savedTheme.isNullOrBlank()) {
            com.example.ui.theme.AppThemeMode.entries.firstOrNull { it.name == savedTheme }?.let { mode ->
                appThemeMode.value = mode
                isPaperMode.value = (mode == com.example.ui.theme.AppThemeMode.ULTRA_PAPER)
            }
        }
    }

    fun setFontScale(context: Context, scale: Float) {
        val clamped = scale.coerceIn(0.85f, 1.45f)
        _fontScaleFactor.value = clamped
        context.getSharedPreferences("general_chat_appearance", Context.MODE_PRIVATE)
            .edit()
            .putFloat("font_scale_factor", clamped)
            .apply()
    }

    fun setZoomEnabled(context: Context, enabled: Boolean) {
        _zoomEnabled.value = enabled
        context.getSharedPreferences("general_chat_appearance", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("zoom_enabled", enabled)
            .apply()
        emitSnackbar(if (enabled) "Ekran Yakınlaştırma (Zoom) Açıldı" else "Ekran Normal Boyuta Döndü")
    }

    fun setZoomScale(context: Context, scale: Float) {
        val clamped = scale.coerceIn(1.05f, 1.40f)
        _zoomScaleFactor.value = clamped
        context.getSharedPreferences("general_chat_appearance", Context.MODE_PRIVATE)
            .edit()
            .putFloat("zoom_scale_factor", clamped)
            .apply()
    }

    // Login Form State
    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    val loginRememberMe = MutableStateFlow(true)
    val loginPasswordVisible = MutableStateFlow(false)

    // Register Form State
    val registerUsername = MutableStateFlow("")
    val registerFullName = MutableStateFlow("")
    val registerAge = MutableStateFlow("18")
    val registerEmail = MutableStateFlow("")
    val registerPassword = MutableStateFlow("")
    val registerConfirmPassword = MutableStateFlow("")
    val registerTermsAccepted = MutableStateFlow(false)
    val registerPasswordVisible = MutableStateFlow(false)
    val showWeakPasswordWarningDialog = MutableStateFlow(false)

    // Global User Directory & Discovery State
    val directorySearchQuery = MutableStateFlow("")
    val directorySearchResults = MutableStateFlow<List<UserEntity>>(emptyList())
    val selectedDirectoryUser = MutableStateFlow<UserEntity?>(null)

    // Forgot Password & Reset Form State
    val forgotEmail = MutableStateFlow("")
    val verifyCodeInput = MutableStateFlow("")
    val newPassword = MutableStateFlow("")
    val newPasswordConfirm = MutableStateFlow("")
    val newPasswordVisible = MutableStateFlow(false)

    // Home / Profile Dialog State
    val showChangePasswordDialog = MutableStateFlow(false)
    val currentPasswordInput = MutableStateFlow("")
    val changeNewPasswordInput = MutableStateFlow("")
    val changePasswordDialogLoading = MutableStateFlow(false)

    // Cross-Device Cloud Identity (Firebase Auth + Credential Manager)
    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (_: Exception) {
        null
    }
    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(firebaseAuth?.currentUser)
    val firebaseUser = _firebaseUser.asStateFlow()

    private val _cloudProfile = MutableStateFlow<CloudHandleProfile?>(null)
    val cloudProfile = _cloudProfile.asStateFlow()

    private val _cloudAuthLoading = MutableStateFlow(false)
    val cloudAuthLoading = _cloudAuthLoading.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _firebaseUser.value = auth.currentUser
        if (auth.currentUser == null) {
            _cloudProfile.value = null
        }
    }

    init {
        firebaseAuth?.addAuthStateListener(authStateListener)
        viewModelScope.launch {
            repository.seedDemoUserIfEmpty()
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth?.removeAuthStateListener(authStateListener)
    }

    fun trySilentGoogleSignIn(context: Context) {
        viewModelScope.launch {
            val user = GoogleAuthHelper.trySilentSignIn(context)
            _firebaseUser.value = user
            if (user != null) {
                syncCloudProfile(context)
            }
        }
    }

    fun signInWithGoogle(context: Context, navigateToChatOnSuccess: Boolean = true) {
        viewModelScope.launch {
            _cloudAuthLoading.value = true
            val authResult = GoogleAuthHelper.signInInteractive(context)
            authResult.onSuccess { user ->
                _firebaseUser.value = user
                val repo = FirestoreChatRepository.create(context)
                val profileResult = repo.ensureUserProfile()
                profileResult.onSuccess { profile ->
                    _cloudProfile.value = profile
                    viewModelScope.launch(Dispatchers.IO) {
                        repo.publishMyE2eePublicKey(context)
                        com.example.util.NetworkConnectivityAndOfflineManager.flushOfflineOutbox(context)
                    }
                    emitSnackbar("Bulut bağlantısı aktif! Kimliğin: @${profile.handle}")
                    if (navigateToChatOnSuccess) {
                        _currentScreen.value = Screen.Chat
                    }
                }.onFailure { err ->
                    emitSnackbar(err.localizedMessage ?: "Bulut profili hazırlanamadı.")
                }
            }.onFailure { err ->
                emitSnackbar(err.localizedMessage ?: "Google ile giriş tamamlanamadı.")
            }
            _cloudAuthLoading.value = false
        }
    }

    fun syncCloudProfile(context: Context, preferredHandle: String? = null) {
        if (firebaseAuth?.currentUser == null) return
        viewModelScope.launch {
            com.example.util.ParentalControlManager.refreshFromFirebase(context)
            val repo = FirestoreChatRepository.create(context)
            val res = repo.ensureUserProfile(preferredHandle = preferredHandle)
            res.onSuccess { profile ->
                _cloudProfile.value = profile
                viewModelScope.launch(Dispatchers.IO) {
                    repo.publishMyE2eePublicKey(context)
                    com.example.util.NetworkConnectivityAndOfflineManager.flushOfflineOutbox(context)
                }
                if (preferredHandle != null) {
                    emitSnackbar("Yeni bulut kimliğin aktif: @${profile.handle}")
                }
            }.onFailure { err ->
                if (preferredHandle != null) {
                    emitSnackbar(err.localizedMessage ?: "Kullanıcı adı güncellenemedi.")
                }
            }
        }
    }

    fun signOutGoogle(context: Context) {
        viewModelScope.launch {
            GoogleAuthHelper.signOut(context)
            _firebaseUser.value = null
            _cloudProfile.value = null
            emitSnackbar("Google bulut oturumu kapatıldı.")
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun togglePaperMode(context: Context? = null) {
        isPaperMode.value = !isPaperMode.value
        val mode = if (isPaperMode.value) com.example.ui.theme.AppThemeMode.ULTRA_PAPER else com.example.ui.theme.AppThemeMode.CYBER_DARK
        appThemeMode.value = mode
        context?.getSharedPreferences("general_chat_appearance", Context.MODE_PRIVATE)
            ?.edit()?.putString("app_theme_mode", mode.name)?.apply()
        emitSnackbar(if (isPaperMode.value) "Kağıt Ekran (Paper Mode) Aktif" else "Siberpunk Moduna Dönüldü")
    }

    fun setAppTheme(mode: com.example.ui.theme.AppThemeMode, context: Context? = null) {
        appThemeMode.value = mode
        isPaperMode.value = (mode == com.example.ui.theme.AppThemeMode.ULTRA_PAPER)
        context?.getSharedPreferences("general_chat_appearance", Context.MODE_PRIVATE)
            ?.edit()?.putString("app_theme_mode", mode.name)?.apply()
        emitSnackbar("Tema Değiştirildi: ${mode.title}")
    }

    fun triggerPanic() {
        _currentScreen.value = Screen.CamouflageMask(panicConfig.value.targetMask)
    }

    fun signIn() {
        val email = loginEmail.value.trim()
        val password = loginPassword.value

        if (email.isBlank() || password.isBlank()) {
            emitSnackbar("Lütfen e-posta ve şifrenizi girin.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signIn(email, password)
            _isLoading.value = false

            result.onSuccess { user ->
                _currentUser.value = user
                loginPassword.value = ""
                _currentScreen.value = Screen.Slogan
                emitSnackbar("Kimlik doğrulandı: ${user.fullName}")
            }.onFailure { error ->
                emitSnackbar(error.message ?: "Giriş yapılamadı.")
            }
        }
    }

    fun signUp() {
        val username = registerUsername.value.trim().removePrefix("@")
        val fullName = registerFullName.value.trim()
        val email = registerEmail.value.trim()
        val password = registerPassword.value
        val confirmPassword = registerConfirmPassword.value

        if (username.length < 3 || username.length > 20 || !username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            emitSnackbar("Kullanıcı adı 3-20 karakter olmalı ve harf, rakam veya alt çizgi içermelidir.")
            return
        }

        if (fullName.isBlank()) {
            emitSnackbar("Lütfen adınızı ve soyadınızı girin.")
            return
        }

        val effectiveEmail = if (email.isBlank()) {
            "${username.lowercase()}@general.local"
        } else {
            if (!SecurityUtils.isValidEmail(email)) {
                emitSnackbar("Lütfen geçerli bir e-posta adresi girin veya kurtarma e-postasını boş bırakın.")
                return
            }
            email
        }

        if (password.length < 6) {
            emitSnackbar("Şifre en az 6 karakter olmalıdır.")
            return
        }

        if (password != confirmPassword) {
            emitSnackbar("Şifreler birbiriyle eşleşmiyor.")
            return
        }

        if (!registerTermsAccepted.value) {
            emitSnackbar("Lütfen kullanım koşullarını kabul edin.")
            return
        }

        val parsedAge = registerAge.value.trim().toIntOrNull()
        if (parsedAge == null || parsedAge !in 5..120) {
            emitSnackbar("Lütfen geçerli bir yaş girin (5-120).")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signUp(username, fullName, effectiveEmail, password)
            _isLoading.value = false

            result.onSuccess { user ->
                com.example.util.ParentalControlManager.setUserAge(parsedAge)
                _currentUser.value = user
                registerPassword.value = ""
                registerConfirmPassword.value = ""
                _currentScreen.value = Screen.Slogan
                emitSnackbar("Hesabınız oluşturuldu: @${user.username} (${user.fullName})")
            }.onFailure { error ->
                emitSnackbar(error.message ?: "Kayıt işlemi başarısız.")
            }
        }
    }

    fun searchDirectory(query: String) {
        directorySearchQuery.value = query
        viewModelScope.launch {
            val results = repository.searchUsers(query)
            directorySearchResults.value = results
        }
    }

    fun selectDirectoryUser(user: UserEntity?) {
        selectedDirectoryUser.value = user
    }

    fun requestPasswordReset() {
        val email = forgotEmail.value.trim()

        if (!SecurityUtils.isValidEmail(email)) {
            emitSnackbar("Lütfen geçerli bir e-posta adresi girin.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.requestPasswordReset(email)
            _isLoading.value = false

            result.onSuccess { code ->
                verifyCodeInput.value = ""
                _currentScreen.value = Screen.VerifyCode(email = email, simulatedCode = code)
                emitSnackbar("Sıfırlama kodu oluşturuldu.")
            }.onFailure { error ->
                emitSnackbar(error.message ?: "Kod gönderilemedi.")
            }
        }
    }

    fun verifyCode(email: String, expectedCode: String) {
        val code = verifyCodeInput.value.trim()

        if (code.length != 6) {
            emitSnackbar("Lütfen 6 haneli kodu eksiksiz girin.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.verifyResetCode(email, code)
            _isLoading.value = false

            result.onSuccess {
                newPassword.value = ""
                newPasswordConfirm.value = ""
                _currentScreen.value = Screen.ResetPassword(email = email, code = code)
            }.onFailure { error ->
                emitSnackbar(error.message ?: "Geçersiz doğrulama kodu.")
            }
        }
    }

    fun resetPassword(email: String, code: String) {
        val newPass = newPassword.value
        val confirmPass = newPasswordConfirm.value

        if (newPass.length < 6) {
            emitSnackbar("Yeni şifre en az 6 karakter olmalıdır.")
            return
        }

        if (newPass != confirmPass) {
            emitSnackbar("Şifreler birbiriyle eşleşmiyor.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.resetPassword(email, code, newPass)
            _isLoading.value = false

            result.onSuccess {
                loginEmail.value = email
                loginPassword.value = ""
                _currentScreen.value = Screen.Login
                emitSnackbar("Şifreniz başarıyla sıfırlandı!")
            }.onFailure { error ->
                emitSnackbar(error.message ?: "Şifre sıfırlanamadı.")
            }
        }
    }

    fun changePassword() {
        val user = _currentUser.value ?: return
        val currentPass = currentPasswordInput.value
        val newPass = changeNewPasswordInput.value

        if (currentPass.isBlank() || newPass.isBlank()) {
            emitSnackbar("Lütfen tüm alanları doldurun.")
            return
        }

        if (newPass.length < 6) {
            emitSnackbar("Yeni şifre en az 6 karakter olmalıdır.")
            return
        }

        viewModelScope.launch {
            changePasswordDialogLoading.value = true
            val result = repository.changePassword(user.id, currentPass, newPass)
            changePasswordDialogLoading.value = false

            result.onSuccess {
                showChangePasswordDialog.value = false
                currentPasswordInput.value = ""
                changeNewPasswordInput.value = ""
                emitSnackbar("Şifreniz başarıyla güncellendi.")
            }.onFailure { error ->
                emitSnackbar(error.message ?: "Şifre güncellenemedi.")
            }
        }
    }

    fun signOut() {
        _currentUser.value = null
        loginPassword.value = ""
        selectedMainTab.value = 0
        _currentScreen.value = Screen.Gate
        emitSnackbar("Güvenli bir şekilde çıkış yapıldı.")
    }

    fun updateDisplayName(
        context: Context,
        newName: String,
        onResult: (Result<String>) -> Unit = {}
    ) {
        val clean = newName.trim().take(80)
        if (clean.isBlank()) {
            val err = IllegalArgumentException("Görünen ad boş olamaz.")
            emitSnackbar(err.message ?: "Hata")
            onResult(Result.failure(err))
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            var finalResult: Result<String> = Result.success(clean)
            val localUser = _currentUser.value
            if (localUser != null) {
                val localRes = repository.updateUserFullName(localUser.id, clean)
                localRes.onSuccess { updated ->
                    _currentUser.value = localUser.copy(fullName = updated)
                }.onFailure { err ->
                    finalResult = Result.failure(err)
                }
            }
            if (firebaseAuth?.currentUser != null) {
                val cloudRes = FirestoreChatRepository.create(context).updateCloudDisplayName(clean)
                cloudRes.onSuccess { profile ->
                    _cloudProfile.value = profile
                    finalResult = Result.success(profile.displayName)
                }.onFailure { err ->
                    finalResult = Result.failure(err)
                }
            }
            _isLoading.value = false
            finalResult.onSuccess {
                emitSnackbar("Görünen adınız güncellendi: $it")
            }.onFailure { err ->
                emitSnackbar(err.localizedMessage ?: "Görünen ad güncellenemedi.")
            }
            onResult(finalResult)
        }
    }

    fun wipeAllData(database: AppDatabase, context: Context? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            database.clearAllTables()
            context?.let { ctx ->
                listOf(
                    "general_chat_appearance",
                    "general_chat_lock_prefs",
                    "general_chat_disguise_prefs",
                    "general_chat_parental_prefs",
                    "general_chat_privacy_prefs"
                ).forEach { prefName ->
                    ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().apply()
                }
                com.example.util.SelfChatStorage.clear(ctx)
                com.example.util.NetworkConnectivityAndOfflineManager.clearAll(ctx)
                com.example.util.E2eeCryptoManager.clearAll(ctx)
            }
            repository.seedDemoUserIfEmpty()
            withContext(Dispatchers.Main) {
                _currentUser.value = null
                loginEmail.value = ""
                loginPassword.value = ""
                appLockPasscode.value = null
                _hasAppLock.value = false
                _isPinLockMode.value = true
                _lockOnBackground.value = true
                _lockOnPanic.value = true
                selectedMainTab.value = 0
                isPaperMode.value = false
                _fontScaleFactor.value = 1.0f
                _zoomEnabled.value = false
                _zoomScaleFactor.value = 1.18f
                _currentScreen.value = Screen.Splash
                emitSnackbar("Bu cihazdaki tüm yerel veriler temizlendi.")
            }
        }
    }

    fun deleteCloudAccountAndAllData(
        context: Context,
        database: AppDatabase,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            var cloudFailure: Throwable? = null
            if (firebaseAuth?.currentUser != null) {
                val cloudDeleteRes = FirestoreChatRepository.create(context).deleteCloudAccountAndOwnedData()
                cloudDeleteRes.onFailure { err ->
                    cloudFailure = if (err is FirebaseAuthRecentLoginRequiredException) {
                        IllegalStateException(
                            "Güvenlik gereği kalıcı hesap silme işlemi için Google oturumunuzu yeniden doğrulamanız gerekir. Lütfen çıkış yapıp tekrar Google ile giriş yaptıktan sonra silmeyi deneyin."
                        )
                    } else {
                        err
                    }
                }
            }

            if (cloudFailure != null) {
                _isLoading.value = false
                emitSnackbar(cloudFailure?.localizedMessage ?: "Bulut hesabı silinemedi.")
                onResult(Result.failure(cloudFailure!!))
                return@launch
            }

            withContext(Dispatchers.IO) {
                database.clearAllTables()
                listOf(
                    "general_chat_appearance",
                    "general_chat_lock_prefs",
                    "general_chat_disguise_prefs",
                    "general_chat_parental_prefs",
                    "general_chat_privacy_prefs"
                ).forEach { prefName ->
                    context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().apply()
                }
                com.example.util.SelfChatStorage.clear(context)
                com.example.util.NetworkConnectivityAndOfflineManager.clearAll(context)
                com.example.util.E2eeCryptoManager.clearAll(context)
            }

            try {
                GoogleAuthHelper.signOut(context)
            } catch (_: Exception) {}

            _firebaseUser.value = null
            _cloudProfile.value = null
            _currentUser.value = null
            loginEmail.value = ""
            loginPassword.value = ""
            appLockPasscode.value = null
            _hasAppLock.value = false
            _isPinLockMode.value = true
            _lockOnBackground.value = true
            _lockOnPanic.value = true
            selectedMainTab.value = 0
            isPaperMode.value = false
            _fontScaleFactor.value = 1.0f
            _zoomEnabled.value = false
            _zoomScaleFactor.value = 1.18f
            _isLoading.value = false
            _currentScreen.value = Screen.Splash
            emitSnackbar("Bulut hesabınız ve bağlı tüm kayıtlarınız kalıcı olarak silindi.")
            onResult(Result.success(Unit))
        }
    }

    /**
     * İMHA Protokolü: Unutulan güvenlik kilidi PIN/şifresini kaldırır,
     * aynı anda bulut hesabını, sohbetleri, yerel veritabanını ve tüm oturumları kalıcı olarak siler.
     */
    fun selfDestructOnForgottenLock(
        context: Context,
        database: AppDatabase,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                runCatching { com.example.util.CloudMessagingManager.stopListening() }
                runCatching { com.example.util.LocalMeshServer.stop() }
                runCatching { com.example.util.LocalMeshServer.stopPeerDiscovery() }
                runCatching { com.example.util.LinkedDevicesManager.clearAllDevices() }
                runCatching { com.example.util.LiveLocationManager.stopAllActiveSessions(context) }

                if (firebaseAuth?.currentUser != null) {
                    runCatching {
                        kotlinx.coroutines.withTimeoutOrNull(15_000L) {
                            FirestoreChatRepository.create(context)
                                .deleteCloudAccountAndOwnedData(allowPartialAuthFallback = true)
                        }
                    }
                }

                runCatching { database.clearAllTables() }
                listOf(
                    "general_chat_appearance",
                    "general_chat_lock_prefs",
                    "general_chat_disguise_prefs",
                    "general_chat_parental_prefs",
                    "general_chat_privacy_prefs",
                    "general_chat_communities",
                    "general_chat_music_prefs",
                    "general_chat_wallpaper_prefs",
                    "general_chat_feedback_prefs"
                ).forEach { prefName ->
                    runCatching {
                        context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().apply()
                    }
                }
                runCatching { SecurityUtils.clearAppLockPasscode(context) }
                runCatching { com.example.util.SelfChatStorage.clear(context) }
                runCatching { com.example.util.NetworkConnectivityAndOfflineManager.clearAll(context) }
                runCatching { com.example.util.E2eeCryptoManager.clearAll(context) }
                runCatching { repository.seedDemoUserIfEmpty() }
                runCatching {
                    kotlinx.coroutines.withTimeoutOrNull(1500L) {
                        GoogleAuthHelper.signOut(context)
                    }
                }
            }

            _firebaseUser.value = null
            _cloudProfile.value = null
            _currentUser.value = null
            loginEmail.value = ""
            loginPassword.value = ""
            appLockPasscode.value = null
            _hasAppLock.value = false
            _isPinLockMode.value = true
            _lockOnBackground.value = true
            _lockOnPanic.value = true
            selectedMainTab.value = 0
            isPaperMode.value = false
            _fontScaleFactor.value = 1.0f
            _zoomEnabled.value = false
            _zoomScaleFactor.value = 1.18f
            _isLoading.value = false
            _currentScreen.value = Screen.Splash
            emitSnackbar("İMHA tamamlandı: Güvenlik kilidi kaldırıldı, tüm sohbetler ve bulut hesabınız silindi.")
            onDone()
        }
    }

    private fun emitSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarChannel.send(message)
        }
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
