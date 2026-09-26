package com.example.ui

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.BuildConfig
import com.example.R
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class GoogleAuthResult {
    data class Success(
        val firebaseUser: FirebaseUser?,
        val email: String,
        val displayName: String,
        val photoUrl: String?
    ) : GoogleAuthResult()

    data class Error(val message: String, val isConfigurationIssue: Boolean = false) : GoogleAuthResult()
    object Cancelled : GoogleAuthResult()
}

/**
 * Await helper for Google Play Services / Firebase Tasks.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}

object FirebaseAuthManager {
    private const val TAG = "FirebaseAuthManager"

    fun getFirebaseAuth(context: Context): FirebaseAuth {
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY.ifBlank { "AIzaSyFakeKeyForDefaultInit" }
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:264405870890:android:financetracker")
                    .setProjectId("ai-studio-financetracker")
                    .setApiKey(apiKey)
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Initialized FirebaseApp programmatically")
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseApp init warning: ${e.message}")
            }
        }
        return FirebaseAuth.getInstance()
    }

    suspend fun signInWithGoogle(context: Context, customServerClientId: String? = null): GoogleAuthResult {
        return try {
            val serverClientId = customServerClientId?.trim()?.ifBlank { null }
                ?: try {
                    context.getString(R.string.default_web_client_id)
                } catch (e: Exception) {
                    "264405870890-financetracker.apps.googleusercontent.com"
                }

            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(context, request)
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Sign in to Firebase Auth with the genuine Google ID token
                val auth = getFirebaseAuth(context)
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).awaitTask()
                val firebaseUser = authResult.user

                val email = firebaseUser?.email ?: googleIdTokenCredential.id
                val name = firebaseUser?.displayName
                    ?: googleIdTokenCredential.displayName
                    ?: email.substringBefore("@")
                val photo = firebaseUser?.photoUrl?.toString()
                    ?: googleIdTokenCredential.profilePictureUri?.toString()

                GoogleAuthResult.Success(
                    firebaseUser = firebaseUser,
                    email = email,
                    displayName = name,
                    photoUrl = photo
                )
            } else {
                GoogleAuthResult.Error("Unsupported credential type received from system: ${credential::class.java.simpleName}")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleAuthResult.Cancelled
        } catch (e: NoCredentialException) {
            GoogleAuthResult.Error("No Google account found on this device. Please sign in to Google in Android Settings first.")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager exception", e)
            val msg = e.localizedMessage ?: e.message ?: "Google Sign-In failed"
            val isConfig = msg.contains("10:") || msg.contains("DEVELOPER_ERROR") || msg.contains("server_client_id") || msg.contains("configuration")
            GoogleAuthResult.Error(msg, isConfigurationIssue = isConfig)
        } catch (e: Exception) {
            Log.e(TAG, "Authentication exception", e)
            GoogleAuthResult.Error(e.localizedMessage ?: e.message ?: "Authentication failed")
        }
    }

    fun signOut(context: Context) {
        try {
            getFirebaseAuth(context).signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out exception", e)
        }
    }
}
