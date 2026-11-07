package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuthRepository
import com.example.data.LanguageOption
import com.example.data.MaskType
import com.example.data.PanicConfig
import com.example.data.SecurityUtils
import com.example.data.UserEntity
import com.example.data.defaultLanguages
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

    // Login Form State
    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    val loginRememberMe = MutableStateFlow(true)
    val loginPasswordVisible = MutableStateFlow(false)

    // Register Form State
    val registerUsername = MutableStateFlow("")
    val registerFullName = MutableStateFlow("")
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

    init {
        viewModelScope.launch {
            repository.seedDemoUserIfEmpty()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun togglePaperMode() {
        isPaperMode.value = !isPaperMode.value
        appThemeMode.value = if (isPaperMode.value) com.example.ui.theme.AppThemeMode.ULTRA_PAPER else com.example.ui.theme.AppThemeMode.CYBER_DARK
        emitSnackbar(if (isPaperMode.value) "Kağıt Ekran (Paper Mode) Aktif" else "Siberpunk Moduna Dönüldü")
    }

    fun setAppTheme(mode: com.example.ui.theme.AppThemeMode) {
        appThemeMode.value = mode
        isPaperMode.value = (mode == com.example.ui.theme.AppThemeMode.ULTRA_PAPER)
        emitSnackbar("Tema Değiştirildi: ${mode.title}")
    }

    fun triggerPanic() {
        _currentScreen.value = Screen.CamouflageMask(panicConfig.value.targetMask)
    }

    fun fillDemoAccount() {
        loginEmail.value = "demo@example.com"
        loginPassword.value = "Demo1234!"
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

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signUp(username, fullName, effectiveEmail, password)
            _isLoading.value = false

            result.onSuccess { user ->
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
        _currentScreen.value = Screen.Gate
        emitSnackbar("Güvenli bir şekilde çıkış yapıldı.")
    }

    fun wipeAllData(database: AppDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            database.clearAllTables()
            repository.seedDemoUserIfEmpty()
            withContext(Dispatchers.Main) {
                _currentUser.value = null
                loginEmail.value = ""
                loginPassword.value = ""
                appLockPasscode.value = null
                isPaperMode.value = false
                _currentScreen.value = Screen.Splash
                emitSnackbar("Tüm veriler imha edildi. Sistem fabrika ayarlarına döndü.")
            }
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
