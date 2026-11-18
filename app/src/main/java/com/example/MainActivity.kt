package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.AuthRepository
import com.example.data.ChatRepository
import com.example.data.CommunitiesRepository
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
import com.example.util.LocalRefreshRateConfig
import com.example.util.RefreshRateManager
import com.example.util.SoundPoolManager
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandated: configure App Check with launch intent before any UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        // Ultra-Smooth 120Hz ProMotion & 1080p FHD+ Display Mode Configuration via RefreshRateManager
        val refreshRateConfig = RefreshRateManager.applyRefreshRate(window, applicationContext)

        // Zero-latency SoundPool initialization
        SoundPoolManager.init(applicationContext)

        // Instant Notification Manager initialization
        com.example.util.InstantNotificationManager.init(applicationContext)

        // Looping Background Music initialization (Stabil Cyber Beat / Chill Ambient)
        com.example.util.BackgroundMusicManager.init(applicationContext)

        // Load persisted custom communities
        CommunitiesRepository.init(applicationContext)

        // Load persisted wallpaper, speech, profanity filter, linked devices & parental control preferences
        com.example.util.ChatWallpaperManager.init(applicationContext)
        com.example.util.GenAiSpeechManager.init(applicationContext)
        com.example.util.ParentalControlManager.init(applicationContext)
        com.example.util.ProfanityFilter.init(applicationContext)
        com.example.util.LinkedDevicesManager.init(applicationContext)
        com.example.util.DisguiseManager.init(applicationContext)
        com.example.util.NetworkConnectivityAndOfflineManager.init(applicationContext)

        val database = AppDatabase.getDatabase(applicationContext)
        val authRepository = AuthRepository(database.userDao())
        val chatRepository = ChatRepository(database.chatDao())

        setContent {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(authRepository)
            )

            LaunchedEffect(Unit) {
                authViewModel.loadAppearancePreferences(applicationContext)
                authViewModel.loadAppLockState(applicationContext)
            }

            val selectedLanguage by authViewModel.selectedLanguage.collectAsState()
            LaunchedEffect(selectedLanguage.code) {
                com.example.util.GenAiSpeechManager.setLanguageCode(selectedLanguage.code)
            }
            val appStrings = remember(selectedLanguage.code) {
                com.example.util.AppTranslations.forCode(selectedLanguage.code)
            }

            val fontScaleFactor by authViewModel.fontScaleFactor.collectAsState()
            val zoomEnabled by authViewModel.zoomEnabled.collectAsState()
            val zoomScaleFactor by authViewModel.zoomScaleFactor.collectAsState()
            val effectiveZoom = if (zoomEnabled) zoomScaleFactor else 1.0f
            val baseDensity = LocalDensity.current
            val scaledDensity = remember(baseDensity, fontScaleFactor, effectiveZoom) {
                Density(
                    density = baseDensity.density * effectiveZoom,
                    fontScale = baseDensity.fontScale * fontScaleFactor
                )
            }

            CompositionLocalProvider(
                LocalRefreshRateConfig provides refreshRateConfig,
                LocalDensity provides scaledDensity,
                com.example.util.LocalAppStrings provides appStrings
            ) {
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
    val hasAppLock by authViewModel.hasAppLock.collectAsState()
    val isPinLockMode by authViewModel.isPinLockMode.collectAsState()
    val lockOnBackground by authViewModel.lockOnBackground.collectAsState()
    val lockOnPanic by authViewModel.lockOnPanic.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        authViewModel.trySilentGoogleSignIn(context)
    }

    // Background Auto-Lock: Triggers 4-digit PIN / Lock screen whenever app goes to background (ON_STOP)
    DisposableEffect(lifecycleOwner, lockOnBackground) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    val suppressed = com.example.data.SecurityUtils.consumeSuppressNextBackgroundLock() ||
                        authViewModel.suppressNextBackgroundLock
                    authViewModel.suppressNextBackgroundLock = false
                    val lockConfigured = com.example.data.SecurityUtils.hasAppLockPasscode(context) ||
                        !authViewModel.appLockPasscode.value.isNullOrBlank()
                    val screen = authViewModel.currentScreen.value
                    val isLockEligibleScreen = screen !is Screen.Splash &&
                        screen !is Screen.Onboarding &&
                        screen !is Screen.LanguagePicker &&
                        screen !is Screen.Gate &&
                        screen !is Screen.Login &&
                        screen !is Screen.Register &&
                        screen !is Screen.ForgotPassword &&
                        screen !is Screen.VerifyCode &&
                        screen !is Screen.ResetPassword &&
                        screen !is Screen.AppLockSetup &&
                        screen !is Screen.AppLockVerify
                    if (!suppressed && lockConfigured && lockOnBackground && isLockEligibleScreen) {
                        authViewModel.navigateTo(Screen.AppLockVerify)
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    authViewModel.suppressNextBackgroundLock = false
                    com.example.data.SecurityUtils.suppressNextBackgroundLock = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
            val lockActive = com.example.data.SecurityUtils.hasAppLockPasscode(context) ||
                !authViewModel.appLockPasscode.value.isNullOrBlank()
            if (lockActive && authViewModel.lockOnPanic.value) {
                authViewModel.navigateTo(Screen.AppLockVerify)
            } else {
                authViewModel.navigateTo(Screen.Chat)
            }
        }
    }

    // Global Panic button -> if user configured a PIN/app lock & lockOnPanic is enabled, lock screen; otherwise apply camouflage screen
    LaunchedEffect(Unit) {
        panicViewModel.panicLockEvents.collect {
            val lockActive = com.example.data.SecurityUtils.hasAppLockPasscode(context) ||
                !authViewModel.appLockPasscode.value.isNullOrBlank()
            if (lockActive && authViewModel.lockOnPanic.value) {
                authViewModel.navigateTo(Screen.AppLockVerify)
            } else {
                authViewModel.navigateTo(Screen.CamouflageMask(panicConfig.targetMask))
            }
        }
    }

    var askedNotificationPermission by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled gracefully; in-app HUD remains active regardless
    }

    val parentalState by com.example.util.ParentalControlManager.state.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            com.example.util.ParentalControlManager.recordUsageMinute()
        }
    }

    LaunchedEffect(currentScreen) {
        com.example.util.ParentalControlManager.logNavigationEvent(
            screenName = currentScreen::class.simpleName ?: "Ekran",
            action = "Açılış"
        )
        if (currentScreen !is Screen.Splash) {
            SoundPoolManager.playMenuNav()
        }
        if (currentScreen is Screen.Chat && !askedNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askedNotificationPermission = true
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (parentalState.isMinorMode && parentalState.isScreenTimeLocked) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {},
            title = {
                androidx.compose.material3.Text(
                    "⏳ Günlük Kullanım Süresi Doldu",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                )
            },
            text = {
                androidx.compose.material3.Text(
                    "18 yaş altı Ebeveyn Denetimi aktif: Günlük ${parentalState.dailyLimitMinutes} dk limitine ulaşıldı (${parentalState.usedMinutesToday} dk kullanıldı). Eşleştirme Kodu: ${parentalState.childPairingCode}"
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = { com.example.util.ParentalControlManager.unlockScreenTimeByParent(30) }
                ) {
                    androidx.compose.material3.Text("Ebeveyn Onayı (+30 dk)")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                        animationSpec = spring(dampingRatio = 0.90f, stiffness = Spring.StiffnessMedium),
                        initialOffsetX = { fullWidth -> fullWidth / 5 }
                    ) + scaleIn(
                        animationSpec = spring(dampingRatio = 0.90f, stiffness = Spring.StiffnessMedium),
                        initialScale = 0.96f
                    ) + fadeIn(animationSpec = tween(140)))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = spring(dampingRatio = 0.90f, stiffness = Spring.StiffnessMedium),
                            targetOffsetX = { fullWidth -> -fullWidth / 5 }
                        ) + scaleOut(
                            animationSpec = spring(dampingRatio = 0.90f, stiffness = Spring.StiffnessMedium),
                            targetScale = 0.97f
                        ) + fadeOut(animationSpec = tween(110))
                    )
                },
                label = "ScreenTransition"
            ) { screen ->
            when (screen) {
                is Screen.Splash -> SplashScreen(
                    onFinished = {
                        val lockActive = com.example.data.SecurityUtils.hasAppLockPasscode(context) ||
                            !authViewModel.appLockPasscode.value.isNullOrBlank()
                        if (lockActive) {
                            authViewModel.navigateTo(Screen.AppLockVerify)
                        } else {
                            authViewModel.navigateTo(Screen.Onboarding)
                        }
                    }
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
                    onGoogleSignIn = { authViewModel.signInWithGoogle(context, navigateToChatOnSuccess = true) },
                    onWipeData = { authViewModel.wipeAllData(database, context) }
                )
                is Screen.Slogan -> SloganScreen(
                    onTriggerShatter = {
                        if (hasAppLock || appLockPasscode != null) {
                            authViewModel.navigateTo(Screen.AppLockVerify)
                        } else {
                            authViewModel.navigateTo(Screen.Chat)
                        }
                    }
                )
                is Screen.AppLockSetup -> AppLockScreen(
                    isSetupMode = true,
                    initialPinMode = true,
                    onLockPassed = { newPasscode ->
                        val isPin = newPasscode.length == 4 && newPasscode.all { it.isDigit() }
                        authViewModel.setAppLockPasscode(context, newPasscode, isPinMode = isPin)
                        authViewModel.navigateTo(Screen.Chat)
                    },
                    onCancel = {
                        authViewModel.navigateTo(Screen.Chat)
                    }
                )
                is Screen.AppLockVerify -> AppLockScreen(
                    isSetupMode = false,
                    initialPinMode = isPinLockMode,
                    currentPasscode = appLockPasscode.orEmpty(),
                    onVerifyPasscode = { entered -> authViewModel.verifyAppLockPasscode(context, entered) },
                    onLockPassed = {
                        authViewModel.navigateTo(Screen.Chat)
                    },
                    onWipeData = {
                        authViewModel.selfDestructOnForgottenLock(context, database)
                    }
                )
                is Screen.Chat -> ChatScreen(
                    chatViewModel = chatViewModel,
                    authViewModel = authViewModel,
                    currentUser = currentUser,
                    isPaperMode = isPaperMode,
                    onTogglePaperMode = { authViewModel.togglePaperMode(context) },
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
                        authViewModel.wipeAllData(database, context)
                    },
                    onDeleteCloudAccount = { onDone ->
                        authViewModel.deleteCloudAccountAndAllData(context, database, onDone)
                    }
                )
                is Screen.MiniGames -> MiniGamesScreen(
                    onBack = { authViewModel.navigateTo(Screen.Chat) }
                )
                is Screen.CamouflageMask -> CamouflageMaskScreen(
                    maskType = screen.maskType,
                    onSecretExit = {
                        panicViewModel.disengagePanic()
                        val lockActive = com.example.data.SecurityUtils.hasAppLockPasscode(context) ||
                            !authViewModel.appLockPasscode.value.isNullOrBlank()
                        if (lockActive && authViewModel.lockOnPanic.value) {
                            authViewModel.navigateTo(Screen.AppLockVerify)
                        } else {
                            authViewModel.navigateTo(Screen.Chat)
                        }
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
                    onWipeAllData = { authViewModel.wipeAllData(database, context) }
                )
                is Screen.AppShowcase -> AppShowcaseScreen(
                    onBack = { authViewModel.navigateTo(Screen.Chat) }
                )
                is Screen.Login -> LoginScreen(
                    viewModel = authViewModel,
                    onWipeData = { authViewModel.wipeAllData(database, context) }
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
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopCenter)
                .statusBarsPadding()
        )
    }
}
}
