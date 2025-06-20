package com.example.codesgram.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.codesgram.model.User
import com.example.codesgram.ui.theme.dimens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

@Composable
fun LoginScreen(navController: NavController) {
    val emailText = rememberSaveable { mutableStateOf("") }
    val passwordText = rememberSaveable { mutableStateOf("") }
    val emailErrorMessage = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }
    val isForgotPasswordMode = remember { mutableStateOf(false) }
    val resetMessage = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFA116), // LeetCode orange
                        Color(0xFFFF8C42), // Lighter orange
                        Color(0xFFFF6B6B), // Coral
                        Color(0xFFFF4D6D)  // Light red/pink
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(top = 20.dp)
    ) {
        Greeting()
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.extraLarge))

        LazyColumn {
            item {
                Email(
                    navController = navController,
                    emailText = emailText,
                    passwordText = passwordText,
                    emailErrorMessage = emailErrorMessage,
                    errorMessage = errorMessage
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium2))

                // Show Forgot Password form or text based on isForgotPasswordMode
                if (isForgotPasswordMode.value) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Automatically use emailText for the reset
                        OutlinedTextField(
                            value = emailText.value, // Use emailText directly
                            onValueChange = {}, // Don't allow editing, just display the email
                            label = { Text("Enter your email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                            readOnly = true, // Make the field read-only
                            isError = emailText.value.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailText.value).matches()
                        )
                        if (emailText.value.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailText.value).matches()) {
                            Text(
                                text = "Please enter a valid email",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (emailText.value.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(emailText.value).matches()) {
                                    isLoading.value = true
                                    auth.sendPasswordResetEmail(emailText.value)
                                        .addOnCompleteListener { task ->
                                            isLoading.value = false
                                            resetMessage.value = if (task.isSuccessful) {
                                                "Password reset email sent. Please check your inbox."
                                            } else {
                                                "Failed to send reset email. Try again later."
                                            }
                                        }
                                } else {
                                    resetMessage.value = "Please enter a valid email."
                                }
                            },
                            modifier = Modifier.fillMaxWidth().align(Alignment.End),
                            enabled = !isLoading.value
                        ) {
                            if (isLoading.value) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Send Reset Link")
                            }
                        }

                        // Display reset password message
                        Spacer(modifier = Modifier.height(16.dp))
                        if (resetMessage.value.isNotEmpty()) {
                            Text(
                                text = resetMessage.value,
                                color = if (resetMessage.value.contains("sent")) Color.Green else Color.Red,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                } else {
                    // Forgot Password clickable text
                    Text(
                        text = "Forgot Password?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 200.dp)
                            .clickable {
                                isForgotPasswordMode.value = true
                            }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium2))
            }
            item {
                CreateAccount(navController = navController)
            }
        }
    }
}


@Composable
fun Greeting(){
    Column(
        modifier =
        Modifier
            .padding(start = 30.dp, top = MaterialTheme.dimens.medium2)
            .fillMaxWidth(0.9f)
    ) {

        Text(
            text = "WELCOME BACK",
            fontSize = 20.sp
        )

        Text(
            text = "Login to your Account",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Email(
    navController: NavController, // Add navController
    emailText: MutableState<String>,
    passwordText: MutableState<String>,
    emailErrorMessage: MutableState<String>,
    errorMessage: MutableState<String>
) {
    val passwordVisibility = rememberSaveable { mutableStateOf(false) }

    Column {
        // Email
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.dimens.medium2),
            trailingIcon = {
                Icon(Icons.Filled.Email, contentDescription = "email")
            },
            value = emailText.value,
            onValueChange = {
                emailText.value = it
                emailErrorMessage.value = validateEmail(it)
            },
            label = {
                Text(
                    text = "Email",
                    fontSize = 18.sp
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.DarkGray,
                focusedLabelColor = Color.Black.copy(.9f),
                unfocusedLabelColor = Color.Gray
            )
        )

        if (emailErrorMessage.value.isNotEmpty()) {
            Text(
                text = emailErrorMessage.value,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = MaterialTheme.dimens.small1)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Password
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.dimens.medium2),
            value = passwordText.value,
            onValueChange = {
                passwordText.value = it
                errorMessage.value = validatePassword(it)
            },
            label = {
                Text(
                    text = "Password",
                    fontSize = 18.sp
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.DarkGray,
                focusedLabelColor = Color.Black.copy(.9f),
                unfocusedLabelColor = Color.Gray
            ),
            visualTransformation = if (passwordVisibility.value) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisibility.value)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = {
                    passwordVisibility.value = !passwordVisibility.value
                }) {
                    Icon(
                        imageVector = image,
                        contentDescription = "show password"
                    )
                }
            }
        )

        if (errorMessage.value.isNotEmpty()) {
            Text(
                text = errorMessage.value,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.extraLarge))

    // Login Button with Navigation on Success
    ContinueButton(emailText.value, passwordText.value, navController)
}


@Composable
fun ContinueButton(
    email: String,
    password: String,
    navController: NavController // Add navController
) {
    val context = LocalContext.current
    val isLoading = remember { mutableStateOf(false) }

    if (isLoading.value) {
        CircularProgressIndicator()
    }
    ElevatedButton(
        onClick = {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password, context, navController)
            } else {
                Log.d("Login", "Please fill in all fields") // Log missing fields
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.dimens.medium2)
            .height(MaterialTheme.dimens.buttonHeight),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black
        )
    ) {
        Text(text = "Login")
    }
}

fun loginUser(
    email: String,
    password: String,
    context: Context,
    navController: NavController
) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid

                if (userId != null) {
                    // Fetch user data from Firebase Realtime Database
                    FirebaseDatabase.getInstance().getReference("users").child(userId).get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val user = snapshot.getValue(User::class.java)

                                // You can save this user data to a ViewModel or pass it to the next screen
                                // For example: ViewModel or SharedPreferences to persist user info

                                // Navigate to the chat screen after loading user data
                                navController.navigate("chat") {
                                    popUpTo("LoginScreen") { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Handle case when user ID is null
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Login failed, show an error message
                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

@Composable
fun CreateAccount(navController: NavController) {
    Row(modifier = Modifier
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center) {
        Text(text = "Don't have an account?",
            fontSize = 18.sp)
        Text(text = "Sign Up",
            textDecoration = TextDecoration.Underline,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                navController.navigate("SignUpScreen") {
                    popUpTo("LoginScreen") { inclusive = true }
                }
            }
        )
    }
}

fun validatePassword(password: String): String{
    val uppercasePattern = Pattern.compile(".*[A-Z].*")
    val digitPattern = Pattern.compile(".*\\d.*")
    val specialCharPattern = Pattern.compile(".*[!@#$%^&*()].*")

    return when{
        password.length < 8 -> "Password must be at least 8 characters long."
        !uppercasePattern.matcher(password).matches() -> "Password must contain at least one uppercase letter."
        !digitPattern.matcher(password).matches() -> "Password must contain at least one digit."
        !specialCharPattern.matcher(password).matches() -> "Password must contain at least one special character."
        else -> ""
    }
}
