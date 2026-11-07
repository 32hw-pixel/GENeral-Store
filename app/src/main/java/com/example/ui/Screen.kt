package com.example.ui

import com.example.data.MaskType
import com.example.data.UserEntity

sealed class Screen {
    data object Splash : Screen()
    data object Onboarding : Screen()
    data object LanguagePicker : Screen()
    data object Gate : Screen()
    data object Slogan : Screen()
    data object AppLockSetup : Screen()
    data object AppLockVerify : Screen()
    data object Chat : Screen()
    data class CamouflageMask(val maskType: MaskType) : Screen()
    data object PanicWizard : Screen()
    data object OfficialChannel : Screen()
    data object AboutSupport : Screen()
    data object AppShowcase : Screen()
    data object MiniGames : Screen()
    data object Login : Screen()
    data object Register : Screen()
    data object ForgotPassword : Screen()
    data class VerifyCode(val email: String, val simulatedCode: String) : Screen()
    data class ResetPassword(val email: String, val code: String) : Screen()
    data class Home(val user: UserEntity) : Screen()
}
