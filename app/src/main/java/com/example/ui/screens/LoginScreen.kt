package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.AuthViewModel
import com.example.ui.Screen
import com.example.ui.components.AuthHeader
import com.example.ui.components.AuthTextField

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onWipeData: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    val email by viewModel.loginEmail.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val rememberMe by viewModel.loginRememberMe.collectAsState()
    val passwordVisible by viewModel.loginPasswordVisible.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val cloudAuthLoading by viewModel.cloudAuthLoading.collectAsState()

    androidx.activity.compose.BackHandler {
        viewModel.navigateTo(Screen.Chat)
    }

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
                    onClick = { viewModel.navigateTo(Screen.Chat) },
                    modifier = Modifier.testTag("login_back_to_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat ve Sohbete Dön"
                    )
                }
            }

            AuthHeader(
                title = "Giriş Yap",
                subtitle = "Farklı cihazlardan internet üzerinden sohbet etmek için Google ile bağlanın",
                icon = Icons.Default.Security
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.signInWithGoogle(context, navigateToChatOnSuccess = true) },
                enabled = !cloudAuthLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_google_signin_button")
            ) {
                if (cloudAuthLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Google ile Bağlan (2 Cihaz Bulut Sohbeti)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AuthTextField(
                value = email,
                onValueChange = { viewModel.loginEmail.value = it },
                label = "Kullanıcı Adı veya E-posta",
                leadingIcon = Icons.Default.Person,
                testTag = "login_email_input",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = password,
                onValueChange = { viewModel.loginPassword.value = it },
                label = "Şifre",
                leadingIcon = Icons.Default.Lock,
                testTag = "login_password_input",
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityToggle = {
                    viewModel.loginPasswordVisible.value = !passwordVisible
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Remember Me & Forgot Password Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { viewModel.loginRememberMe.value = it },
                        modifier = Modifier.testTag("login_remember_me_checkbox")
                    )
                    Text(
                        text = "Beni Hatırla",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.forgotEmail.value = email
                        viewModel.navigateTo(Screen.ForgotPassword)
                    },
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text(
                        text = "Şifremi Unuttum?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.signIn() },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Giriş Yap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hesabınız yok mu?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.navigateTo(Screen.Register) },
                    modifier = Modifier.testTag("go_to_register_button")
                ) {
                    Text(
                        text = "Hemen Kayıt Olun",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.Chat) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("login_continue_as_guest_button")
            ) {
                Text(
                    text = "Giriş Yapmadan Sohbete Başla",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (onWipeData != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { showWipeConfirmDialog = true },
                    modifier = Modifier.testTag("login_wipe_data_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Text(
                        text = "Tüm Hesap Verilerini Sıfırla (Temiz Başlangıç)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hesap Verilerini Sıfırla",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bu cihazda oluşturduğunuz yerel hesaplar ve şifreler kalıcı olarak silinecektir. Bu işlemden sonra yeni bir hesap açabilir veya Google hesabınızla bağlanabilirsiniz. Devam edilsin mi?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWipeConfirmDialog = false
                        onWipeData?.invoke()
                    },
                    modifier = Modifier.testTag("login_confirm_wipe_button")
                ) {
                    Text(
                        text = "Evet, Sıfırla",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWipeConfirmDialog = false }
                ) {
                    Text(text = "Vazgeç")
                }
            },
            shape = RoundedCornerShape(18.dp)
        )
    }
}
