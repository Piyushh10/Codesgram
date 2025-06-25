package com.example.codegram.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.codegram.model.leetcode.Submission
import com.example.codegram.ui.theme.dimens
import com.example.codegram.viewmodel.LeetCodeStatsViewModel
import com.example.codegram.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LeetCodeStatsScreen(viewModel: LeetCodeStatsViewModel, username: String, viewmodel: UserProfileViewModel) {
    val leetCodeStats by viewModel.leetCodeStats.observeAsState()
    val userProfileStats by viewmodel.userProfile.observeAsState()
    val contestRating by viewModel.contestRating.observeAsState()
    val recentSubmission by viewModel.recentSubmissions.observeAsState(emptyList())

    LaunchedEffect(username) {
        Log.d("LeetCodeStatsScreen", "Username: $username")
        viewModel.fetchLeetCodeStats(username)
        viewModel.fetchRating(username)
        viewmodel.fetchUserProfile(username)
        viewModel.fetchRecentSubmissions(username)

        // Update group in Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                val leetCodeStats = com.example.codegram.retrofit.ApiService.leetCodeApi.getProfile(username)
                val userProfile = com.example.codegram.retrofit.ApiService.leetCodeApi.userProfile(username)
                val rating = userProfile.ranking
                val totalSolved = leetCodeStats.totalSolved
                val group = when {
                    (rating in 0..1400) || (totalSolved in 0..200) -> "Beginner"
                    (rating in 1401..1700) || (totalSolved in 201..400) -> "Intermediate"
                    else -> "Advanced"
                }
                val dbGrp = FirebaseDatabase.getInstance().getReference("groups").child(userId).child("groupName")
                dbGrp.setValue(group)
            } catch (e: Exception) {
                Log.e("LeetCodeStatsScreen", "Failed to update group in Firebase", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(start = 15.dp, top = MaterialTheme.dimens.large)
    ) {
        leetCodeStats?.let { stats ->
            userProfileStats?.let { profile ->
                contestRating?.let { contest ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .clip(RoundedCornerShape(16.dp)) // Rounded corners for the Box) // Orange shadow
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth() // Padding around the Card
                                .background(Color.Black) // Card background black
                        ) {
                            Column(
                                modifier = Modifier // Padding inside the Card
                                    .background(Color.Black) // Ensure card content also has black background
                            ) {
                                // Profile Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                ) {
                                    AsyncImage(
                                        model = userProfileStats?.avatar,
                                        contentDescription = "",
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = Color.White // Set text color to white
                                        )
                                        Spacer(Modifier.height(5.dp))
                                        Row {
                                            InfoCard("Rank: ${stats.ranking}")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            InfoCard("Rating: ${contest.contestRating}")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Questions Solved Section
                                SectionTitle("Questions Solved")
                                Text("${stats.totalSolved}/${stats.totalQuestions}", fontSize = 16.sp, color = Color.White) // Set text color to white
                                Spacer(modifier = Modifier.height(8.dp))

                                SectionTitle("Easy Solved")
                                Text("${stats.easySolved}/${stats.totalEasy}", fontSize = 16.sp, color = Color.White) // Set text color to white
                                Spacer(modifier = Modifier.height(8.dp))

                                SectionTitle("Medium Solved")
                                Text("${stats.mediumSolved}/${stats.totalMedium}", fontSize = 16.sp, color = Color.White) // Set text color to white
                                Spacer(modifier = Modifier.height(8.dp))

                                SectionTitle("Hard Solved")
                                Text("${stats.hardSolved}/${stats.totalHard}", fontSize = 16.sp, color = Color.White) // Set text color to white
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color.White, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Recent Submissions Section
                                SectionTitle("Recent Submissions")
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color.White, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                    item {
                                        if (recentSubmission.isNotEmpty()) {
                                            recentSubmission.forEachIndexed { index, submission ->
                                                SubmissionCard(submission)
                                                if (index != recentSubmission.lastIndex) {
                                                    Divider(color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                                                }
                                                Spacer(Modifier.padding(bottom = 10.dp))
                                            }
                                        } else {
                                            Text("No recent submissions available.", color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } ?: Text("Loading...", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = when {
            text == "Easy Solved" -> Color(0xFF27ae60) // Green for easy
            text == "Medium Solved" -> Color(0xFFf39c12) // Yellow for medium
            text == "Hard Solved" -> Color.Red // Red for hard
            else -> Color.White // Default text color for other sections
        }
    )
}

@Composable
fun SubmissionCard(submission: Submission) {
    fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getDefault()
        return sdf.format(java.util.Date(timestamp * 1000))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .clip(RoundedCornerShape(8.dp)) // Add padding for the shadow
    ) {
        Text("Title: ${submission.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Status: ${submission.statusDisplay}", fontSize = 14.sp, color = Color.White)
        Text("Language: ${submission.lang}", fontSize = 14.sp, color = Color.White)
        Text("Submitted: ${formatTimestamp(submission.timestamp)}", fontSize = 14.sp, color = Color.White)
    }
}

@Composable
fun InfoCard(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Color.White
    )
}

