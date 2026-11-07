package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.AuthViewModel
import com.example.ui.Screen
import com.example.ui.components.AuthHeader
import com.example.ui.components.AuthTextField
import com.example.ui.components.PasswordStrengthIndicator

@Composable
fun ResetPasswordScreen(
    email: String,
    code: String,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Login)
    }

    val newPassword by viewModel.newPassword.collectAsState()
    val confirmPassword by viewModel.newPasswordConfirm.collectAsState()
    val passwordVisible by viewModel.newPasswordVisible.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Login) },
                    modifier = Modifier.testTag("reset_password_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri dön"
                    )
                }
            }

            AuthHeader(
                title = "Yeni Şifre Belirleyin",
                subtitle = "Hesabınız için güçlü ve yeni bir şifre oluşturun",
                icon = Icons.Default.Password
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                value = newPassword,
                onValueChange = { viewModel.newPassword.value = it },
                label = "Yeni Şifre",
                leadingIcon = Icons.Default.Lock,
                testTag = "reset_new_password_input",
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityToggle = {
                    viewModel.newPasswordVisible.value = !passwordVisible
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            PasswordStrengthIndicator(password = newPassword)

            Spacer(modifier = Modifier.height(16.dp))

            val passwordMismatch = confirmPassword.isNotEmpty() && newPassword != confirmPassword
            AuthTextField(
                value = confirmPassword,
                onValueChange = { viewModel.newPasswordConfirm.value = it },
                label = "Yeni Şifreyi Onayla",
                leadingIcon = Icons.Default.Lock,
                testTag = "reset_confirm_password_input",
                isPassword = true,
                passwordVisible = passwordVisible,
                isError = passwordMismatch,
                errorMessage = if (passwordMismatch) "Şifreler eşleşmiyor" else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.resetPassword(email, code) },
                enabled = !isLoading && newPassword.length >= 6 && newPassword == confirmPassword,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("reset_password_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = "Şifreyi Güncelle ve Giriş Yap",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
