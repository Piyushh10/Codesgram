package com.example.codesgram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.codesgram.chat.ChatHelper
import com.example.codesgram.ui.navigation.AccountNavigation
import com.example.codesgram.viewmodel.LeetCodeStatsViewModel
import com.example.codesgram.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val userViewModel: UserProfileViewModel by viewModels()
    private val leetCodeViewModel:LeetCodeStatsViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var chatHelper: ChatHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
        AccountNavigation()
//        LeetCodeProfileScreen(username = "vir_s_ingh")
//        UserProfileScreen(userViewModel, "raghavvbagdi")
        }
    }
}