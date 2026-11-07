package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.AuthRepository
import com.example.data.ChatRepository
import com.example.ui.AuthViewModel
import com.example.ui.AuthViewModelFactory
import com.example.ui.ChatViewModel
import com.example.ui.ChatViewModelFactory
import com.example.ui.Screen
import com.example.ui.screens.AboutSupportScreen
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.AppShowcaseScreen
import com.example.ui.screens.CamouflageMaskScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.GateScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguagePickerScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MiniGamesScreen
import com.example.ui.screens.OfficialChannelScreen
import com.example.ui.screens.OnboardingSlidesScreen
import com.example.ui.screens.PanicConfigScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.ResetPasswordScreen
import com.example.ui.screens.SloganScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VerifyCodeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.SoundPoolManager
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandated: configure App Check with launch intent before any UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        // Ultra-Smooth 120Hz ProMotion & 1080p FHD+ Display Mode Configuration
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val display = windowManager.defaultDisplay
                val modes = display.supportedModes
                // Prioritize modes that provide 1080p (physicalWidth >= 1080 or physicalHeight >= 1920) with maximum refresh rate (up to 120Hz)
                val fhd1080pModes = modes.filter { it.physicalWidth >= 1080 || it.physicalHeight >= 1920 }
                val targetMode = if (fhd1080pModes.isNotEmpty()) {
                    fhd1080pModes.maxByOrNull { it.refreshRate }
                } else {
                    modes.maxByOrNull { it.refreshRate }
                }

                if (targetMode != null) {
                    val params = window.attributes
                    params.preferredDisplayModeId = targetMode.modeId
                    window.attributes = params
                }
            }
        } catch (_: Exception) {}

        // Zero-latency SoundPool initialization
        SoundPoolManager.init(applicationContext)

        // Instant Notification Manager initialization
        com.example.util.InstantNotificationManager.init(applicationContext)

        // Looping Background Music initialization (Stabil Cyber Beat / Chill Ambient)
        com.example.util.BackgroundMusicManager.init(applicationContext)

        val database = AppDatabase.getDatabase(applicationContext)
        val authRepository = AuthRepository(database.userDao())
        val chatRepository = ChatRepository(database.chatDao())

        setContent {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(authRepository)
            )

            val panicViewModel: com.example.ui.PanicViewModel = viewModel(
                factory = com.example.ui.PanicViewModelFactory(applicationContext, chatRepository)
            )

            val isPaperMode by authViewModel.isPaperMode.collectAsState()
            val appThemeMode by authViewModel.appThemeMode.collectAsState()

            MyApplicationTheme(themeMode = appThemeMode, paperMode = isPaperMode) {
                val currentUser by authViewModel.currentUser.collectAsState()

                val chatViewModel: ChatViewModel = viewModel(
                    key = currentUser?.id?.toString() ?: "guest",
                    factory = ChatViewModelFactory(chatRepository, currentUser?.id)
                )

                MainAppContent(
                    authViewModel = authViewModel,
                    chatViewModel = chatViewModel,
                    panicViewModel = panicViewModel,
                    database = database
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        com.example.util.BackgroundMusicManager.stop()
    }
}

@Composable
fun MainAppContent(
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    panicViewModel: com.example.ui.PanicViewModel,
    database: AppDatabase
) {
    val currentScreen by authViewModel.currentScreen.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isPaperMode by authViewModel.isPaperMode.collectAsState()
    val panicConfig by authViewModel.panicConfig.collectAsState()
    val appLockPasscode by authViewModel.appLockPasscode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        authViewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Hardware shake / biometric panic events
    LaunchedEffect(Unit) {
        panicViewModel.panicTriggerEvents.collect { targetMask ->
            authViewModel.navigateTo(Screen.CamouflageMask(targetMask))
        }
    }

    LaunchedEffect(Unit) {
        panicViewModel.panicDisengageEvents.collect {
            authViewModel.navigateTo(Screen.Chat)
        }
    }

    // Global Panic button -> instant lock with passcode/biometric
    LaunchedEffect(Unit) {
        panicViewModel.panicLockEvents.collect {
            if (authViewModel.appLockPasscode.value.isNullOrBlank()) {
                authViewModel.appLockPasscode.value = "1234"
            }
            authViewModel.navigateTo(Screen.AppLockVerify)
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen !is Screen.Splash) {
            SoundPoolManager.playMenuNav()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (slideInHorizontally(
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                        initialOffsetX = { fullWidth -> fullWidth / 4 }
                    ) + scaleIn(
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                        initialScale = 0.94f
                    ) + fadeIn(animationSpec = tween(180)))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                            targetOffsetX = { fullWidth -> -fullWidth / 4 }
                        ) + scaleOut(
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                            targetScale = 0.96f
                        ) + fadeOut(animationSpec = tween(140))
                    )
                },
                label = "ScreenTransition"
            ) { screen ->
            when (screen) {
                is Screen.Splash -> SplashScreen(
                    onFinished = { authViewModel.navigateTo(Screen.Onboarding) }
                )
                is Screen.Onboarding -> OnboardingSlidesScreen(
                    onFinished = { authViewModel.navigateTo(Screen.LanguagePicker) }
                )
                is Screen.LanguagePicker -> LanguagePickerScreen(
                    onLanguageSelected = { option ->
                        authViewModel.selectedLanguage.value = option
                        authViewModel.navigateTo(Screen.Gate)
                    }
                )
                is Screen.Gate -> GateScreen(
                    onRegister = { authViewModel.navigateTo(Screen.Register) },
                    onLogin = { authViewModel.navigateTo(Screen.Login) },
                    onGuest = { authViewModel.navigateTo(Screen.Slogan) },
                    onWipeData = { authViewModel.wipeAllData(database) }
                )
                is Screen.Slogan -> SloganScreen(
                    onTriggerShatter = {
                        if (appLockPasscode != null) {
                            authViewModel.navigateTo(Screen.AppLockVerify)
                        } else {
                            authViewModel.navigateTo(Screen.Chat)
                        }
                    }
                )
                is Screen.AppLockSetup -> AppLockScreen(
                    isSetupMode = true,
                    onLockPassed = { newPasscode ->
                        authViewModel.appLockPasscode.value = newPasscode
                        authViewModel.navigateTo(Screen.Chat)
                    }
                )
                is Screen.AppLockVerify -> AppLockScreen(
                    isSetupMode = false,
                    currentPasscode = appLockPasscode.orEmpty(),
                    onLockPassed = {
                        authViewModel.navigateTo(Screen.Chat)
                    },
                    onResetLock = {
                        authViewModel.appLockPasscode.value = null
                        authViewModel.navigateTo(Screen.Chat)
                    },
                    onWipeData = {
                        authViewModel.wipeAllData(database)
                    }
                )
                is Screen.Chat -> ChatScreen(
                    chatViewModel = chatViewModel,
                    authViewModel = authViewModel,
                    currentUser = currentUser,
                    isPaperMode = isPaperMode,
                    onTogglePaperMode = { authViewModel.togglePaperMode() },
                    onTriggerPanic = { panicViewModel.triggerPanicOverride() },
                    onTriggerGlobalPanic = { panicViewModel.triggerGlobalPanicLock() },
                    onSelectMask = { mask -> authViewModel.navigateTo(Screen.CamouflageMask(mask)) },
                    onOpenChannel = { authViewModel.navigateTo(Screen.OfficialChannel) },
                    onOpenAppLock = { authViewModel.navigateTo(Screen.AppLockSetup) },
                    onOpenAbout = { authViewModel.navigateTo(Screen.AboutSupport) },
                    onOpenAuth = {
                        if (currentUser != null) {
                            authViewModel.navigateTo(Screen.Home(currentUser!!))
                        } else {
                            authViewModel.navigateTo(Screen.Login)
                        }
                    },
                    onOpenShowcase = {
                        authViewModel.navigateTo(Screen.AppShowcase)
                    },
                    onOpenMiniGames = {
                        authViewModel.navigateTo(Screen.MiniGames)
                    },
                    onReplaySplash = {
                        authViewModel.navigateTo(Screen.Splash)
                    },
                    onWipeAllData = {
                        authViewModel.wipeAllData(database)
                    }
                )
                is Screen.MiniGames -> MiniGamesScreen(
                    onBack = { authViewModel.navigateTo(Screen.Chat) }
                )
                is Screen.CamouflageMask -> CamouflageMaskScreen(
                    maskType = screen.maskType,
                    onSecretExit = {
                        panicViewModel.disengagePanic()
                        authViewModel.navigateTo(Screen.Chat)
                    }
                )
                is Screen.PanicWizard -> PanicConfigScreen(
                    currentConfig = panicConfig,
                    onTestPanicOverride = { mask ->
                        panicViewModel.triggerPanicOverride()
                    },
                    onConfigCompleted = { newConfig ->
                        panicViewModel.applyConfig(newConfig)
                        authViewModel.panicConfig.value = newConfig
                        authViewModel.navigateTo(Screen.Chat)
                    },
                    onCancel = { authViewModel.navigateTo(Screen.Chat) }
                )
                is Screen.OfficialChannel -> OfficialChannelScreen(
                    onBack = { authViewModel.navigateTo(Screen.Chat) }
                )
                is Screen.AboutSupport -> AboutSupportScreen(
                    onBack = { authViewModel.navigateTo(Screen.Chat) },
                    onWipeAllData = { authViewModel.wipeAllData(database) }
                )
                is Screen.AppShowcase -> AppShowcaseScreen(
                    onBack = { authViewModel.navigateTo(Screen.Chat) }
                )
                is Screen.Login -> LoginScreen(
                    viewModel = authViewModel,
                    onWipeData = { authViewModel.wipeAllData(database) }
                )
                is Screen.Register -> RegisterScreen(viewModel = authViewModel)
                is Screen.ForgotPassword -> ForgotPasswordScreen(viewModel = authViewModel)
                is Screen.VerifyCode -> VerifyCodeScreen(
                    email = screen.email,
                    simulatedCode = screen.simulatedCode,
                    viewModel = authViewModel
                )
                is Screen.ResetPassword -> ResetPasswordScreen(
                    email = screen.email,
                    code = screen.code,
                    viewModel = authViewModel
                )
                is Screen.Home -> HomeScreen(
                    user = screen.user,
                    viewModel = authViewModel
                )
            }
        }

        com.example.ui.components.InstantNotificationHud(
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
        )
    }
}
}
