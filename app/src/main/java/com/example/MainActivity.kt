package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.data.repository.FirestoreRepository
import com.example.ui.MainAppContent
import com.example.ui.theme.NijoPvtTheme
import com.example.ui.theme.SlateBackground
import com.example.util.configureAppCheck
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            NijoPvtTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SlateBackground
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    // Listen to Firebase Auth state
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Attempt silent background Google TV sign-in without blocking UI
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            try {
                val clientId = context.getString(R.string.default_web_client_id)
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(true)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val cred = result.credential
                if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val idToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
                    val authCred = GoogleAuthProvider.getCredential(idToken, null)
                    Firebase.auth.signInWithCredential(authCred).await()
                    Log.d(TAG, "Silent Google TV sign-in successful: ${Firebase.auth.currentUser?.email}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "Background sign-in bypassed for TV workspace: ${e.message}")
            }
        }
    }

    // Launch directly into the NIJO PVT TV workspace without blocking manual login screen
    val repository = remember(context) {
        FirestoreRepository(context)
    }

    MainAppContent(
        repository = repository,
        onSignOutRequested = {
            currentUser = null
        }
    )
}
