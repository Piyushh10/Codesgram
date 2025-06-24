package com.example.codegram.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.codegram.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.codegram.retrofit.ApiService
import com.example.codegram.retrofit.LeetCodeRepository

@Composable
fun LeetCodeUsernameInputScreen(navController: NavController) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val userId = firebaseUser?.uid ?: ""
    val email = firebaseUser?.email ?: ""
    val googleAvatar = firebaseUser?.photoUrl?.toString() ?: "https://assets.leetcode.com/users/default_avatar.jpg"
    val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
    val leetCodeRepository = remember { LeetCodeRepository(ApiService.leetCodeApi) }
    var alreadySet by remember { mutableStateOf(false) }

    // Check if username is already set
    LaunchedEffect(userId) {
        dbRef.child("username").get().addOnSuccessListener {
            val existing = it.getValue(String::class.java)
            if (!existing.isNullOrBlank()) {
                alreadySet = true
                // Optionally, navigate to chat directly
                navController.navigate("chat") {
                    popUpTo("LeetCodeUsernameInputScreen") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    if (alreadySet) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("LeetCode username already set for this account.", color = Color.Red)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter your LeetCode username", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("LeetCode Username") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (username.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val userProfile = leetCodeRepository.fetchUserProfile(username)
                        val avatar = userProfile?.avatar ?: googleAvatar
                        val userMap = mapOf(
                            "userId" to userId,
                            "username" to username,
                            "email" to email,
                            "avatar" to avatar,
                            "rating" to (userProfile?.ranking ?: 0)
                        )
                        dbRef.setValue(userMap).addOnSuccessListener {
                            navController.navigate("chat") {
                                popUpTo("LeetCodeUsernameInputScreen") { inclusive = true }
                                launchSingleTop = true
                            }
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to save user info", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Continue")
            }
        }
    }
} 