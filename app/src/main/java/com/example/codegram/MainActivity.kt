package com.example.codegram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.codegram.chat.ChatHelper
import com.example.codegram.ui.navigation.AccountNavigation
import com.example.codegram.viewmodel.LeetCodeStatsViewModel
import com.example.codegram.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    private val userViewModel: UserProfileViewModel by viewModels()
    private val leetCodeViewModel:LeetCodeStatsViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var chatHelper: ChatHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            AccountNavigation()
//        LeetCodeProfileScreen(username = "vir_s_ingh")
//        UserProfileScreen(userViewModel, "raghavvbagdi")
        }
    }
}