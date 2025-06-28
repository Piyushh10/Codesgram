package com.example.codegram.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codegram.model.leetcode.Submission
import com.example.codegram.model.leetcode.UserProfile
import com.example.codegram.ui.theme.dimens
import com.example.codegram.viewmodel.LeetCodeStatsViewModel
import com.example.codegram.retrofit.ApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LeetCodeStatsScreen(viewModel: LeetCodeStatsViewModel, username: String) {
    val leetCodeStats by viewModel.leetCodeStats.observeAsState()
    val contestRating by viewModel.contestRating.observeAsState()
    val recentSubmission by viewModel.recentSubmissions.observeAsState(emptyList())
    var userProfile by remember { mutableStateOf<com.example.codegram.model.leetcode.UserProfile?>(null) }

    LaunchedEffect(username) {
        viewModel.fetchLeetCodeStats(username)
        viewModel.fetchRating(username)
        viewModel.fetchRecentSubmissions(username)
        try {
            userProfile = ApiService.leetCodeApi.userProfile(username)
        } catch (_: Exception) {
            userProfile = null
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
        if (leetCodeStats == null || contestRating == null || userProfile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading...", color = Color.White, fontSize = 18.sp)
            }
        } else {
            val stats = leetCodeStats!!
            val contest = contestRating!!
            val profile = userProfile!!
            val country = profile.country.orEmpty()
            val school = profile.school.orEmpty()
            val gitHub = profile.gitHub.orEmpty()
            val linkedIN = profile.linkedIN.orEmpty()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Profile Header Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2F4C).copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = profile.avatar,
                                    contentDescription = "Profile Avatar",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, Color(0xFF7AB2D3), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = username,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        StatChip("Rank", "${stats.ranking}")
                                        StatChip("Rating", "${contest.contestRating?.toInt() ?: "-"}")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            // Profile Info
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (country.isNotBlank()) ProfileInfoItem("Location", country)
                                if (school.isNotBlank()) ProfileInfoItem("University", school)
                                if (gitHub.isNotBlank()) ProfileLinkItem("GitHub", gitHub)
                                if (linkedIN.isNotBlank()) ProfileLinkItem("LinkedIn", linkedIN)
                            }
                        }
                    }
                }
                // Questions Solved Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2F4C).copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Questions Solved",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${stats.totalSolved}/${stats.totalQuestions}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF7AB2D3)
                                )
                            }
                            DifficultyProgress("Easy", stats.easySolved, stats.totalEasy, Color(0xFF27ae60))
                            Spacer(modifier = Modifier.height(8.dp))
                            DifficultyProgress("Medium", stats.mediumSolved, stats.totalMedium, Color(0xFFf39c12))
                            Spacer(modifier = Modifier.height(8.dp))
                            DifficultyProgress("Hard", stats.hardSolved, stats.totalHard, Color(0xFFe74c3c))
                        }
                    }
                }
                // Recent Submissions Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2F4C).copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Recent Submissions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            if (recentSubmission.isNotEmpty()) {
                                recentSubmission.forEachIndexed { index, submission ->
                                    ModernSubmissionCard(submission)
                                    if (index != recentSubmission.lastIndex) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No recent submissions available",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFF1B1F3A).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF7AB2D3)
        )
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B1F3A).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileLinkItem(label: String, url: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B1F3A).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .combinedClickable(
                onClick = {
                    // Open in browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                onLongClick = {
                    // Copy to clipboard
                    clipboardManager.setText(AnnotatedString(url))
                    Toast.makeText(context, "$label link copied!", Toast.LENGTH_SHORT).show()
                }
            )
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF7AB2D3),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = url,
            fontSize = 14.sp,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
fun DifficultyProgress(difficulty: String, solved: Int, total: Int, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B1F3A).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = difficulty,
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                text = "$solved/$total",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(4.dp))
                    .width(
                        (if (total == 0) 0f else (solved.toFloat() / total.toFloat()))
                            .coerceIn(0f, 1f)
                            .let { it * 100 }
                            .dp
                    )
            )
        }
    }
}

@Composable
fun ModernSubmissionCard(submission: com.example.codegram.model.leetcode.Submission) {
    fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getDefault()
        return sdf.format(java.util.Date(timestamp * 1000))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1F3A).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = submission.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = submission.statusDisplay,
                    fontSize = 14.sp,
                    color = when {
                        submission.statusDisplay.contains("Accepted") -> Color(0xFF27ae60)
                        submission.statusDisplay.contains("Wrong") -> Color(0xFFe74c3c)
                        else -> Color(0xFFf39c12)
                    }
                )
                Text(
                    text = submission.lang,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(submission.timestamp),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

