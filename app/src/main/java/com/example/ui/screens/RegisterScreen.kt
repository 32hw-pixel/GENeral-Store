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
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.CyberYellow

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.Login)
    }

    val username by viewModel.registerUsername.collectAsState()
    val fullName by viewModel.registerFullName.collectAsState()
    val email by viewModel.registerEmail.collectAsState()
    val password by viewModel.registerPassword.collectAsState()
    val confirmPassword by viewModel.registerConfirmPassword.collectAsState()
    val termsAccepted by viewModel.registerTermsAccepted.collectAsState()
    val passwordVisible by viewModel.registerPasswordVisible.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showWeakPasswordDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Roadmap Section 5: Weak Password Warning Dialog ("EMİN MİSİN?")
    if (showWeakPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showWeakPasswordDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Güvenlik Uyarısı",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "GÜVENLİK PROTOKOLÜ: EMİN MİSİN?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "Belirlediğiniz şifre ('$password') zayıf güvenlik düzeyinde. Siberpunk şifreleme ve küresel frekans güvenliği için en az 8 karakter, büyük harf ve rakam önerilir.\n\nBu şifreyle kalenizi kurmak istediğinizden kesinlikle emin misiniz?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWeakPasswordDialog = false
                        viewModel.signUp()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_weak_password_button")
                ) {
                    Text("Evet, Riski Al ve Devam Et", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWeakPasswordDialog = false },
                    modifier = Modifier.testTag("cancel_weak_password_button")
                ) {
                    Text("Şifreyi Güçlendir")
                }
            }
        )
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
            // Top back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Login) },
                    modifier = Modifier.testTag("register_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri dön"
                    )
                }
            }

            AuthHeader(
                title = "Kişisel Kalenizi Kurun",
                subtitle = "Benzersiz Kullanıcı ID'nizi belirleyin ve şifreli ağa katılın",
                icon = Icons.Default.PersonAdd
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Unique User ID (No duplicates permitted)
            AuthTextField(
                value = username,
                onValueChange = { input ->
                    val sanitized = input.replace(" ", "").lowercase()
                    viewModel.registerUsername.value = sanitized
                },
                label = "Benzersiz Kullanıcı ID'si (@kullanici_id)",
                leadingIcon = Icons.Default.AlternateEmail,
                testTag = "register_username_input",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Text(
                text = "⚡ Bu ID dünyada sadece size aittir (aynı ID'yi 2. bir kişi alamaz). Arkadaşlarınız sizi bu ID ile bulup doğrulayacak.",
                style = MaterialTheme.typography.labelSmall,
                color = CyberYellow,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 4.dp, bottom = 12.dp)
            )

            AuthTextField(
                value = fullName,
                onValueChange = { viewModel.registerFullName.value = it },
                label = "Ad Soyad (Görünen İsim)",
                leadingIcon = Icons.Default.Person,
                testTag = "register_fullname_input",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            AuthTextField(
                value = email,
                onValueChange = { viewModel.registerEmail.value = it },
                label = "Kurtarma E-postası (İsteğe Bağlı)",
                leadingIcon = Icons.Default.Email,
                testTag = "register_email_input",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Text(
                text = "🛡️ İsteğe bağlıdır. Dilerseniz boş bırakabilir veya gelecekte şifrenizi kurtarmak için ekleyebilirsiniz.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 4.dp, bottom = 12.dp)
            )

            AuthTextField(
                value = password,
                onValueChange = { viewModel.registerPassword.value = it },
                label = "Şifre",
                leadingIcon = Icons.Default.Lock,
                testTag = "register_password_input",
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityToggle = {
                    viewModel.registerPasswordVisible.value = !passwordVisible
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            PasswordStrengthIndicator(password = password)

            Spacer(modifier = Modifier.height(16.dp))

            val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
            AuthTextField(
                value = confirmPassword,
                onValueChange = { viewModel.registerConfirmPassword.value = it },
                label = "Şifreyi Onayla",
                leadingIcon = Icons.Default.Lock,
                testTag = "register_confirm_password_input",
                isPassword = true,
                passwordVisible = passwordVisible,
                isError = passwordMismatch,
                errorMessage = if (passwordMismatch) "Şifreler eşleşmiyor" else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { viewModel.registerTermsAccepted.value = it },
                    modifier = Modifier.testTag("register_terms_checkbox")
                )
                Text(
                    text = "Kullanım koşullarını ve gizlilik politikasını kabul ediyorum.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val isWeak = password.length < 8 || password.all { it.isDigit() } || password.all { it.isLetter() }
                    if (password.isNotEmpty() && isWeak) {
                        showWeakPasswordDialog = true
                    } else {
                        viewModel.signUp()
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Kayıt Ol",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Zaten bir hesabınız var mı?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.navigateTo(Screen.Login) },
                    modifier = Modifier.testTag("go_to_login_button")
                ) {
                    Text(
                        text = "Giriş Yapın",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
