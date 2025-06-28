package com.example.codegram.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val googleAvatar = firebaseUser?.photoUrl?.toString()
        ?: "https://assets.leetcode.com/users/default_avatar.jpg"
    val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
    val leetCodeRepository = remember { LeetCodeRepository(ApiService.leetCodeApi) }
    var alreadySet by remember { mutableStateOf(false) }

    // Check if username is already set
    LaunchedEffect(userId) {
        dbRef.child("username").get().addOnSuccessListener {
            val existing = it.getValue(String::class.java)
            if (!existing.isNullOrBlank()) {
                alreadySet = true
                navController.navigate("chat") {
                    popUpTo("LeetCodeUsernameInputScreen") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0F27), Color(0xFF1B1F3A))
                )
            )
            .systemBarsPadding()
    ) {
        if (alreadySet) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("LeetCode username already set.", color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Enter Your LeetCode Username",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We use this to fetch your profile & stats",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("LeetCode Username", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (username.isNotBlank()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val userProfile = leetCodeRepository.fetchUserProfile(username)
                                val avatar = userProfile?.avatar ?: googleAvatar
                                val rating = userProfile?.ranking ?: 0
                                val leetCodeStats = ApiService.leetCodeApi.getProfile(username)
                                val totalSolved = leetCodeStats.totalSolved
                                val group = when {
                                    (rating in 0..1400) || (totalSolved in 0..200) -> "Beginner"
                                    (rating in 1401..1700) || (totalSolved in 201..400) -> "Intermediate"
                                    else -> "Advanced"
                                }
                                val userMap = mapOf(
                                    "userId" to userId,
                                    "username" to username,
                                    "email" to email,
                                    "avatar" to avatar,
                                    "rating" to rating,
                                    "totalSolved" to totalSolved,
                                    "group" to group
                                )
                                dbRef.setValue(userMap).addOnSuccessListener {
                                    navController.navigate("chat") {
                                        popUpTo("LeetCodeUsernameInputScreen") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Failed to save user info",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter a username",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "Continue",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Your stats will be visible to other users in chats",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
