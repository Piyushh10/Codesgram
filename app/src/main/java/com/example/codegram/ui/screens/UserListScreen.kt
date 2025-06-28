@file:Suppress("UNUSED_EXPRESSION")

package com.example.codegram.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.codegram.chat.ChatHelper
import com.example.codegram.model.User
import com.example.codegram.model.leetcode.groups
import com.example.codegram.ui.navigation.Screen
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun UserListScreen(
    chatHelper: ChatHelper,
    currentUserId: String,
    currentUserAvatar: String?, // Add a parameter for the current user's avatar
    onUserClick: (User) -> Unit,
    navController: NavController
) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF2B5876), Color(0xFF4E4376)),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    val db = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("username")
    var username by remember { mutableStateOf("") }

    LaunchedEffect(currentUserId) {
        db.get().addOnSuccessListener { snapshot->
            username = snapshot.getValue(String::class.java).toString()?: "Unknown"
        }
    }

    Column(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0F27), Color(0xFF1B1F3A))
                )
            )
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        LaunchedEffect(Unit) {
            chatHelper.fetchUser { fetchedUsers ->
                val filteredUsers = fetchedUsers.filter { it.userId != currentUserId }
                users = filteredUsers
            }
        }

        LazyColumn {
            items(users) { user ->
                UserItem(user = user, onClick = { onUserClick(user) })
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Modern Chat Button
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2F4C)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.size(64.dp)
            ) {
                IconButton(
                    onClick = { null },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.ChatBubble,
                        contentDescription = "Chat",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF7AB2D3)
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Modern Group Button
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2F4C)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.size(64.dp)
            ) {
                IconButton(
                    onClick = {
                        val selectedGroup = groups.firstOrNull()
                        selectedGroup?.let {
                            navController.navigate(Screen.GroupChat.createRoute(it.name))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Group",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF7AB2D3)
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Modern Avatar with border and shadow
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2F4C)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.size(56.dp)
            ) {
                AsyncImage(
                    model = currentUserAvatar,
                    contentDescription = "Current User Avatar",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF7AB2D3), CircleShape)
                        .clickable {
                            navController.navigate(Screen.LeetCodeStats.createRoute(username))
                        }
                )
            }
        }
    }
}

@Composable
fun UserItem(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2F4C).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with modern styling
            AsyncImage(
                model = user.avatar,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF7AB2D3), CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.username,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Tap to start chatting",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            // Chat indicator
            Icon(
                Icons.Default.ChatBubble,
                contentDescription = "Chat",
                tint = Color(0xFF7AB2D3),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}





