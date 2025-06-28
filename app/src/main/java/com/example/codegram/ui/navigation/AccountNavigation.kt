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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.codegram.R
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.codegram.ui.screens.LeaderboardScreen

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
    var leetCodeUsername by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            startDestination = Screen.Login.route
        } else {
            val userId = user.uid
            val dbRef = db.getReference("users").child(userId)
            dbRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("leetCodeUsername").getValue(String::class.java)
                    ?: snapshot.child("username").getValue(String::class.java)
                leetCodeUsername = username
                startDestination = if (!username.isNullOrBlank()) {
                    Screen.Chat.route
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
        val currentBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentRoute = currentBackStackEntry?.destination?.route
        val showBottomBar = currentRoute == Screen.Chat.route ||
            (currentRoute?.startsWith("LeetCodeStatsScreen") == true) ||
            (currentRoute == Screen.Leaderboard.route)
        if (currentRoute?.startsWith("PersonalChatScreen") == true) {
            Scaffold(
                bottomBar = {},
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination!!,
                    modifier = Modifier.padding(innerPadding)
                ) {
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
                        route = Screen.PersonalChat.route,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { navBackStackEntry ->
                        val userId = navBackStackEntry.arguments?.getString("userId")
                        if (userId != null) {
                            ChatScreen(
                                chatHelper = ChatHelper(db, LocalContext.current),
                                navController = navController,
                                personalChatUserId = userId
                            )
                        }
                    }
                    composable(
                        route = Screen.GroupChat.route,
                        arguments = listOf(navArgument("groupName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val groupName = backStackEntry.arguments?.getString("groupName")
                        groupName?.let {
                            val chatHelper = ChatHelper(db, LocalContext.current)
                            val selectedGroup = com.example.codegram.model.leetcode.groups.find { it.name == groupName }
                            if (selectedGroup != null) {
                                GroupChatScreen(group = selectedGroup, chatHelper = chatHelper, navController = navController)
                            }
                        }
                    }
                    composable(Screen.Leaderboard.route) {
                        LeaderboardScreen()
                    }
                }
            }
            return
        }
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = Color(0xFF1B1F3A)
                    ) {
                        // Leaderboard first
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                            label = { Text("Leetboard") },
                            selected = currentRoute == Screen.Leaderboard.route,
                            onClick = {
                                navController.navigate(Screen.Leaderboard.route) {
                                    popUpTo(Screen.Chat.route)
                                    launchSingleTop = true
                                }
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF7AB2D3),
                                unselectedIconColor = Color(0xFFB0B3C7),
                                selectedTextColor = Color(0xFF7AB2D3),
                                unselectedTextColor = Color(0xFFB0B3C7),
                                indicatorColor = Color.Transparent
                            )
                        )
                        // Group second
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Group, contentDescription = "Group") },
                            label = { Text("Group") },
                            selected = currentRoute?.startsWith("GroupChatScreen") == true,
                            onClick = {
                                val group = com.example.codegram.model.leetcode.groups.firstOrNull()
                                group?.let {
                                    navController.navigate(Screen.GroupChat.createRoute(it.name)) {
                                        popUpTo(Screen.Chat.route)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF7AB2D3),
                                unselectedIconColor = Color(0xFFB0B3C7),
                                selectedTextColor = Color(0xFF7AB2D3),
                                unselectedTextColor = Color(0xFFB0B3C7),
                                indicatorColor = Color.Transparent
                            )
                        )
                        // Chat third
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                            label = { Text("Chat") },
                            selected = currentRoute?.startsWith(Screen.Chat.route) == true,
                            onClick = {
                                navController.navigate(Screen.Chat.route) {
                                    popUpTo(Screen.Chat.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF7AB2D3),
                                unselectedIconColor = Color(0xFFB0B3C7),
                                selectedTextColor = Color(0xFF7AB2D3),
                                unselectedTextColor = Color(0xFFB0B3C7),
                                indicatorColor = Color.Transparent
                            )
                        )
                        // Profile fourth
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentRoute?.startsWith("LeetCodeStatsScreen") == true,
                            onClick = {
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                val currentUserId = currentUser?.uid
                                if (currentUserId != null) {
                                    val dbRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId)
                                    dbRef.child("leetCodeUsername").get().addOnSuccessListener { snapshot ->
                                        val username = snapshot.getValue(String::class.java)
                                        if (!username.isNullOrBlank()) {
                                            navController.navigate(Screen.LeetCodeStats.createRoute(username)) {
                                                popUpTo(Screen.Chat.route)
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF7AB2D3),
                                unselectedIconColor = Color(0xFFB0B3C7),
                                selectedTextColor = Color(0xFF7AB2D3),
                                unselectedTextColor = Color(0xFFB0B3C7),
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination!!,
                modifier = Modifier.padding(innerPadding)
            ) {
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
                    route = Screen.PersonalChat.route,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { navBackStackEntry ->
                    val userId = navBackStackEntry.arguments?.getString("userId")
                    if (userId != null) {
                        ChatScreen(
                            chatHelper = ChatHelper(db, LocalContext.current),
                            navController = navController,
                            personalChatUserId = userId
                        )
                    }
                }
                composable(
                    route = Screen.GroupChat.route,
                    arguments = listOf(navArgument("groupName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val groupName = backStackEntry.arguments?.getString("groupName")
                    groupName?.let {
                        val chatHelper = ChatHelper(db, LocalContext.current)
                        val selectedGroup = com.example.codegram.model.leetcode.groups.find { it.name == groupName }
                        if (selectedGroup != null) {
                            GroupChatScreen(group = selectedGroup, chatHelper = chatHelper, navController = navController)
                        }
                    }
                }
                composable(Screen.Leaderboard.route) {
                    LeaderboardScreen()
                }
            }
        }
    }
}







