@file:Suppress("UNUSED_EXPRESSION")

package com.example.codegram.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.LocalTextStyle
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
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val db = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("username")
    var username by remember { mutableStateOf("") }
    var lastUserId by remember { mutableStateOf<String?>(null) }
    var hasMore by remember { mutableStateOf(true) }
    val pageSize = 20

    LaunchedEffect(currentUserId) {
        db.get().addOnSuccessListener { snapshot->
            username = snapshot.getValue(String::class.java).toString()?: "Unknown"
        }
    }

    // Pagination: Load first page on launch
    LaunchedEffect(Unit) {
        users = emptyList()
        lastUserId = null
        hasMore = true
        chatHelper.fetchUsersPaginated(pageSize, null) { fetchedUsers, newLastKey ->
            users = fetchedUsers.filter { it.userId != currentUserId }
            lastUserId = newLastKey
            hasMore = newLastKey != null
        }
    }

    fun loadMoreUsers() {
        if (!hasMore) return
        chatHelper.fetchUsersPaginated(pageSize, lastUserId) { fetchedUsers, newLastKey ->
            val newUsers = fetchedUsers.filter { it.userId != currentUserId }
            users = users + newUsers
            lastUserId = newLastKey
            hasMore = newLastKey != null
        }
    }

    // --- Enhanced Search Filtering ---
    val filteredUsers = if (searchQuery.isBlank()) {
        users
    } else {
        val query = searchQuery.trim().lowercase()
        users.filter { user ->
            user.username.lowercase().contains(query) ||
            user.email.lowercase().contains(query)
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
        // Always-visible Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2F4C).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF7AB2D3),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search users...", color = Color.White.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .align(Alignment.CenterVertically),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 15.sp),
                    singleLine = true
                )
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Clear Search",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn {
            if (filteredUsers.isEmpty() && searchQuery.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2F4C).copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found for '$searchQuery'",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredUsers) { user ->
                    UserItem(user = user, onClick = { onUserClick(user) })
                }
                // Pagination: Load More button
                if (hasMore && searchQuery.isBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                onClick = { loadMoreUsers() },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF7AB2D3)),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    text = "Load More",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
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





