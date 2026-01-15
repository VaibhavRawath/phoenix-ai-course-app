package com.example.sayit

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(onSignInSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameToConfirm by remember { mutableStateOf("") }

    // Configure Google Sign In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("229345191542-brhdq71o24trdo9s60eehl930l30qd72.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    scope.launch {
                        isLoading = true
                        val authResult = firebaseAuthWithGoogle(token)
                        isLoading = false
                        if (authResult) {
                            // Fetch current name and show dialog
                            val user = Firebase.auth.currentUser
                            nameToConfirm = user?.displayName ?: ""
                            showNameDialog = true
                        } else {
                            Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: ApiException) {
                Log.w("SignIn", "Google sign in failed", e)
                Toast.makeText(context, "Google Sign In Failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        } else {
             Log.e("SignIn", "Sign-in result unsuccessful, resultCode: ${result.resultCode}")
             Toast.makeText(context, "Sign-in cancelled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF101025),
            title = { Text("Confirm Your Name", color = Color.White) },
            text = {
                Column {
                    Text("How should we call you?", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nameToConfirm,
                        onValueChange = { nameToConfirm = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color(0xFFD500F9),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFFD500F9)
                        ),
                        label = { Text("Display Name", color = Color.Gray) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (nameToConfirm.isNotBlank()) {
                                isLoading = true
                                updateUserProfile(nameToConfirm)
                                isLoading = false
                                showNameDialog = false
                                onSignInSuccess()
                            } else {
                                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD500F9))
                ) {
                    Text("Save & Continue", color = Color.White)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050510)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Say It",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isLoading) {
                 CircularProgressIndicator(color = Color(0xFFD500F9))
                 Spacer(modifier = Modifier.height(8.dp))
                 Text("Processing...", color = Color.White)
            } else {
                Button(
                    onClick = {
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign in with Google", color = Color.Black)
                }
            }
        }
    }
}

suspend fun firebaseAuthWithGoogle(idToken: String): Boolean {
    return try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential).await()
        true
    } catch (e: Exception) {
        Log.e("SignIn", "Firebase Auth Failed", e)
        false
    }
}

suspend fun updateUserProfile(name: String) {
    val user = Firebase.auth.currentUser ?: return
    val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(name)
        .build()
    try {
        user.updateProfile(profileUpdates).await()
    } catch (e: Exception) {
        Log.e("SignIn", "Failed to update profile", e)
    }
}
