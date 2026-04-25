package com.ade.habittracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ade.habittracker.ui.theme.AccentYellow
import com.ade.habittracker.ui.theme.CardBackground
import com.ade.habittracker.ui.theme.HabitTrackerTheme
import com.ade.habittracker.ui.theme.TextColorPrimary
import com.ade.habittracker.ui.theme.TextColorSecondary
import com.ade.habittracker.ui.viewmodel.HabitViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class LoginActivity : FragmentActivity() {

    private val viewModel: HabitViewModel by viewModels()
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HabitTrackerTheme {
                var isLoading by remember { mutableStateOf(false) }
                var hasAutoNavigated by remember { mutableStateOf(false) }
                val isLoginServiceReady = remember { FirebaseApp.initializeApp(this@LoginActivity) != null }

                LaunchedEffect(isLoginServiceReady) {
                    if (!hasAutoNavigated && isLoginServiceReady && firebaseAuth.currentUser != null) {
                        hasAutoNavigated = true
                        val user = firebaseAuth.currentUser
                        if (user?.isEmailVerified == true) {
                            viewModel.onAuthenticated()
                            goToMainActivityWithBiometric()
                        } else {
                            user?.sendEmailVerification()
                            firebaseAuth.signOut()
                            viewModel.onUnauthenticated()
                            Toast.makeText(
                                this@LoginActivity,
                                "Email belum diverifikasi. Link verifikasi sudah dikirim ulang.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                LoginScreen(
                    isLoading = isLoading,
                    isLoginServiceReady = isLoginServiceReady,
                    onAuthRequest = authRequest@{ email, password, username, isRegister ->
                        if (!isLoginServiceReady || isLoading) return@authRequest

                        isLoading = true
                        val task = if (isRegister) {
                            firebaseAuth.createUserWithEmailAndPassword(email, password)
                        } else {
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                        }

                        task.addOnCompleteListener(this@LoginActivity) { result ->
                            isLoading = false
                            if (!result.isSuccessful) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    authErrorMessage(result.exception),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@addOnCompleteListener
                            }

                            val currentUser = firebaseAuth.currentUser
                            if (currentUser == null) {
                                Toast.makeText(this@LoginActivity, "Gagal membaca sesi akun.", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }

                            if (isRegister) {
                                currentUser.sendEmailVerification()
                                firebaseAuth.signOut()
                                viewModel.onUnauthenticated()
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Akun berhasil dibuat. Cek email untuk verifikasi sebelum login.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@addOnCompleteListener
                            }

                            if (!currentUser.isEmailVerified) {
                                currentUser.sendEmailVerification()
                                firebaseAuth.signOut()
                                viewModel.onUnauthenticated()
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Email belum diverifikasi. Link verifikasi sudah dikirim.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@addOnCompleteListener
                            }

                            val suggestedName = username.ifBlank {
                                email.substringBefore("@").ifBlank { "Petualang" }
                            }
                            viewModel.onAuthenticated(suggestedUserName = suggestedName)
                            goToMainActivityWithBiometric()
                        }
                    },
                    onForgotPassword = { email ->
                        if (!isLoginServiceReady || isLoading) return@LoginScreen
                        val normalizedEmail = normalizeEmail(email)

                        if (normalizedEmail.isBlank()) {
                            Toast.makeText(this@LoginActivity, "Isi email dulu untuk reset password.", Toast.LENGTH_SHORT).show()
                            return@LoginScreen
                        }
                        if (!isValidEmail(normalizedEmail)) {
                            Toast.makeText(this@LoginActivity, "Format email belum valid.", Toast.LENGTH_SHORT).show()
                            return@LoginScreen
                        }

                        isLoading = true
                        firebaseAuth.sendPasswordResetEmail(normalizedEmail)
                            .addOnCompleteListener(this@LoginActivity) { result ->
                                isLoading = false
                                if (result.isSuccessful) {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Link reset password sudah dikirim. Cek inbox atau folder spam email.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        authErrorMessage(result.exception),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                )
            }
        }
    }

    private fun goToMainActivityWithBiometric() {
        val authenticatorFlags =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuthenticate = BiometricManager.from(this).canAuthenticate(authenticatorFlags)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            goToMainActivity()
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Konfirmasi Identitas")
            .setSubtitle("Verifikasi cepat sebelum masuk aplikasi")
            .setAllowedAuthenticators(authenticatorFlags)
            .build()

        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    goToMainActivity()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        this@LoginActivity,
                        "Verifikasi dibatalkan. Silakan login ulang saat siap.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        biometricPrompt.authenticate(promptInfo)
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun LoginScreen(
    isLoading: Boolean,
    isLoginServiceReady: Boolean,
    onAuthRequest: (email: String, password: String, username: String, isRegister: Boolean) -> Unit,
    onForgotPassword: (email: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cardScale by animateFloatAsState(targetValue = if (isRegisterMode) 1.02f else 1f, label = "scale")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .scale(cardScale)
                .zIndex(1f)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .animateContentSize(animationSpec = tween(durationMillis = 280)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = isRegisterMode,
                    transitionSpec = {
                        fadeIn(tween(180)) + slideInVertically { it / 3 } togetherWith
                            fadeOut(tween(140)) + slideOutVertically { -it / 4 }
                    },
                    label = "authHeaderTransition"
                ) { registerMode ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (registerMode) "DAFTAR AKUN" else "LOGIN",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentYellow,
                            letterSpacing = 1.5.sp
                        )

                        Text(
                            text = if (registerMode) {
                                "Buat akun baru untuk mulai perjalananmu."
                            } else {
                                "Masuk untuk lanjutkan progres harianmu."
                            },
                            fontSize = 14.sp,
                            color = TextColorSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )
                    }
                }

                if (!isLoginServiceReady) {
                    Text(
                        text = "Layanan login belum siap. Coba lagi sebentar.",
                        color = Color(0xFFFFB74D),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isRegisterMode,
                    enter = fadeIn(tween(220)) + expandVertically(animationSpec = tween(220)),
                    exit = fadeOut(tween(120)) + shrinkVertically(animationSpec = tween(180))
                ) {
                    Column {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Nama Petualang") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = loginFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = loginFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Kata Sandi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = loginFieldColors(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done
                    )
                )

                AnimatedVisibility(
                    visible = !isRegisterMode,
                    enter = fadeIn(tween(160)),
                    exit = fadeOut(tween(100))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        Text(
                            text = "Lupa kata sandi?",
                            fontSize = 12.sp,
                            color = AccentYellow,
                            modifier = Modifier.clickable(enabled = !isLoading) {
                                onForgotPassword(email.trim())
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isRegisterMode,
                    enter = fadeIn(tween(220)) + slideInVertically(initialOffsetY = { -it / 3 }),
                    exit = fadeOut(tween(120)) + slideOutVertically(targetOffsetY = { -it / 3 })
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Ulangi Kata Sandi") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            colors = loginFieldColors(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        val normalizedEmail = normalizeEmail(email)
                        val normalizedUsername = username.trim()

                        if (normalizedEmail.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Email dan password wajib diisi.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isValidEmail(normalizedEmail)) {
                            Toast.makeText(context, "Format email belum valid.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isRegisterMode && normalizedUsername.isBlank()) {
                            Toast.makeText(context, "Nama petualang wajib diisi saat daftar.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (password.length < 6) {
                            Toast.makeText(context, "Password minimal 6 karakter.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isRegisterMode && password != confirmPassword) {
                            Toast.makeText(context, "Konfirmasi password tidak cocok.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        onAuthRequest(
                            normalizedEmail,
                            password,
                            if (isRegisterMode) normalizedUsername else "",
                            isRegisterMode
                        )
                    },
                    enabled = !isLoading && isLoginServiceReady,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentYellow,
                        contentColor = Color.Black
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.2.dp,
                            modifier = Modifier.height(20.dp)
                        )
                    } else {
                        AnimatedContent(
                            targetState = isRegisterMode,
                            transitionSpec = {
                                fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                            },
                            label = "authButtonTextTransition"
                        ) { registerMode ->
                            Text(
                                text = if (registerMode) "DAFTAR & MASUK" else "MASUK",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isRegisterMode) "Sudah punya akun? " else "Belum punya akun? ",
                        fontSize = 12.sp,
                        color = TextColorSecondary
                    )
                    Text(
                        text = if (isRegisterMode) "Masuk" else "Daftar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow,
                        modifier = Modifier
                            .clickable(enabled = !isLoading) {
                                isRegisterMode = !isRegisterMode
                                username = ""
                                password = ""
                                confirmPassword = ""
                            }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentYellow,
    unfocusedBorderColor = TextColorSecondary,
    focusedLabelColor = AccentYellow,
    unfocusedLabelColor = TextColorSecondary,
    cursorColor = AccentYellow,
    focusedTextColor = TextColorPrimary,
    unfocusedTextColor = TextColorPrimary
)

private fun authErrorMessage(error: Exception?): String {
    return when (error) {
        is FirebaseAuthInvalidCredentialsException -> "Email atau password tidak valid."
        is FirebaseAuthInvalidUserException -> "Akun tidak ditemukan."
        is FirebaseAuthUserCollisionException -> "Email sudah terdaftar."
        is FirebaseTooManyRequestsException -> "Terlalu banyak percobaan. Coba lagi nanti."
        else -> error?.localizedMessage ?: "Autentikasi gagal."
    }
}

private fun normalizeEmail(email: String): String {
    return email.trim().lowercase()
}

private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
