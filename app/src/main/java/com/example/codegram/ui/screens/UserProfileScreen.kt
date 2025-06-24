package com.example.codegram.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codegram.viewmodel.UserProfileViewModel

@Composable
fun UserProfileScreen(viewModel: UserProfileViewModel, username: String) {
    val userProfile by viewModel.userProfile.observeAsState()
    val userBadges by viewModel.userBadges.observeAsState()

    LaunchedEffect(username) {
        viewModel.fetchUserProfile(username)
        viewModel.fetchUserBadges(username)
    }

    Column {
        userProfile?.let { profile ->
            Text("Ranking: ${profile.ranking}",
                fontSize = 29.sp)
            Text("Reputation: ${profile.reputation}")
        // other profile fields
        } ?: "Profile Loading"

        Spacer(Modifier.height(8.dp))

        userBadges?.let { badges ->
            Text("Badges:")
            badges.forEach { badge ->
                Text("- ${badge.name}: ${badge.description}")
            }
        }
    }
}