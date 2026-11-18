package com.example.util

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

object GoogleAuthHelper {
    private const val TAG = "GoogleAuthHelper"

    suspend fun signInInteractive(context: Context): Result<FirebaseUser> {
        return try {
            val webClientId = context.getString(R.string.default_web_client_id)
            val credentialManager = CredentialManager.create(context)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user
                    ?: return Result.failure(IllegalStateException("Google oturumu açıldı ancak kullanıcı nesnesi boş."))
                Result.success(firebaseUser)
            } else {
                Result.failure(IllegalStateException("Desteklenmeyen kimlik doğrulama türü."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In cancelled or certificate/credential mismatch", e)
            Result.failure(IllegalStateException("Google ile giriş iptal edildi. Hesap seçip tekrar deneyin.", e))
        } catch (e: NoCredentialException) {
            Result.failure(IllegalStateException("Bu cihazda kullanılabilir bir Google hesabı bulunamadı.", e))
        } catch (e: Exception) {
            Log.e(TAG, "Interactive Google Sign-In failed", e)
            Result.failure(IllegalStateException(e.localizedMessage ?: "Google ile giriş başarısız oldu.", e))
        }
    }

    suspend fun trySilentSignIn(context: Context): FirebaseUser? {
        FirebaseAuth.getInstance().currentUser?.let { return it }
        return try {
            val webClientId = context.getString(R.string.default_web_client_id)
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val tokenCred = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(tokenCred.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await().user
            } else {
                null
            }
        } catch (e: NoCredentialException) {
            null
        } catch (e: Exception) {
            Log.d(TAG, "Silent sign-in skipped: ${e.message}")
            null
        }
    }

    suspend fun signOut(context: Context) {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signOut error: ${e.message}")
        }
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w(TAG, "CredentialManager clearCredentialState error: ${e.message}")
        }
    }
}
