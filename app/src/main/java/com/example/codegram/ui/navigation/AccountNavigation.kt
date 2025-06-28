package com.example.codegram.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.codegram.chat.ChatHelper
import com.example.codegram.model.leetcode.groups
import com.example.codegram.ui.screens.ChatScreen
import com.example.codegram.ui.screens.GroupChatScreen
import com.example.codegram.ui.screens.LeetCodeStatsScreen
import com.example.codegram.ui.screens.LeetCodeUsernameInputScreen
import com.example.codegram.ui.screens.LoginScreen
import com.example.codegram.viewmodel.LeetCodeStatsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.codegram.R

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0F27), Color(0xFF1B1F3A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.launcher),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = Color(0xFF7AB2D3))
        }
    }
}

@Composable
fun AccountNavigation() {
    val navController = rememberNavController()
    val db = FirebaseDatabase.getInstance()
    val leetcodeViewModel = LeetCodeStatsViewModel()
    val firebaseAuth = FirebaseAuth.getInstance()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            startDestination = Screen.Login.route
        } else {
            // Try to get the username from Firebase
            val userId = user.uid
            val dbRef = db.getReference("users").child(userId).child("leetCodeUsername")
            dbRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.getValue(String::class.java)
                startDestination = if (!username.isNullOrBlank()) {
                    Screen.LeetCodeStats.createRoute(username)
                } else {
                    "LeetCodeUsernameInputScreen"
                }
            }.addOnFailureListener {
                startDestination = "LeetCodeUsernameInputScreen"
            }
        }
    }

    if (startDestination == null) {
        SplashScreen()
    } else {
        NavHost(navController = navController, startDestination = startDestination!!) {
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable("LeetCodeUsernameInputScreen") { LeetCodeUsernameInputScreen(navController) }
            composable(
                route = Screen.LeetCodeStats.route,
                arguments = listOf(navArgument("username") { type = NavType.StringType })
            ) { navBackStackEntry ->
                val username = navBackStackEntry.arguments?.getString("username")
                if (username != null) {
                    LeetCodeStatsScreen(viewModel = leetcodeViewModel, username = username)
                }
            }
            composable(Screen.Chat.route) {
                ChatScreen(
                    chatHelper = ChatHelper(db, LocalContext.current),
                    navController = navController
                )
            }
            composable(
                route = Screen.GroupChat.route,
                arguments = listOf(navArgument("groupName") { type = NavType.StringType })
            ) { backStackEntry ->
                val groupName = backStackEntry.arguments?.getString("groupName")
                groupName?.let {
                    val chatHelper = ChatHelper(db, LocalContext.current)
                    val selectedGroup = groups.find { it.name == groupName }
                    if (selectedGroup != null) {
                        GroupChatScreen(group = selectedGroup, chatHelper = chatHelper, navController = navController)
                    }
                }
            }
        }
    }
}







