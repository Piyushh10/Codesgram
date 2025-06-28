package com.example.codegram.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.codegram.chat.ChatHelper
import com.example.codegram.retrofit.ApiService
import com.example.codegram.retrofit.LeetCodeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen() {
    val context = LocalContext.current
    val chatHelper = remember { ChatHelper(com.google.firebase.database.FirebaseDatabase.getInstance(), context) }
    val leetCodeRepository = remember { LeetCodeRepository(ApiService.leetCodeApi) }
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        error = null
        chatHelper.fetchUser { users ->
            scope.launch(Dispatchers.IO) {
                val entries = users.map { user ->
                    val leetCodeUsername = user.username // IMPORTANT: This must be the actual LeetCode username!
                    try {
                        val stats = ApiService.leetCodeApi.getProfile(leetCodeUsername)
                        val totalSolved = stats.totalSolved // This is the correct field
                        val avatar = user.avatar
                        LeaderboardEntry(
                            username = leetCodeUsername,
                            avatar = avatar,
                            totalSolved = totalSolved,
                            fetchFailed = false
                        )
                    } catch (e: Exception) {
                        // Show a Toast for debugging (remove in production)
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Failed for $leetCodeUsername: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        LeaderboardEntry(
                            username = leetCodeUsername,
                            avatar = user.avatar,
                            totalSolved = 0,
                            fetchFailed = true
                        )
                    }
                }.sortedByDescending { it.totalSolved }
                leaderboard = entries
                loading = false
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
    ) {
        when {
            loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = Color(0xFF7AB2D3))
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = error ?: "Unknown error",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        itemsIndexed(leaderboard) { index, entry ->
                            LeaderboardRow(rank = index + 1, entry = entry)
                        }
                    }
                }
            }
        }
    }
}

data class LeaderboardEntry(
    val username: String,
    val avatar: String?,
    val totalSolved: Int,
    val fetchFailed: Boolean = false
)

@Composable
fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    val borderColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.Transparent
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2F4C).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B1F3A))
                    .then(if (borderColor != Color.Transparent) Modifier.border(3.dp, borderColor, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = entry.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.username,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = if (entry.fetchFailed) "Solved: N/A" else "Solved: ${entry.totalSolved}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (entry.fetchFailed) Color.Red.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.titleMedium,
                color = borderColor.takeIf { it != Color.Transparent } ?: Color(0xFF7AB2D3)
            )
        }
    }
} 