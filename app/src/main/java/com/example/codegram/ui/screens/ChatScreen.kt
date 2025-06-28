package com.example.codegram.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.codegram.R
import com.example.codegram.chat.ChatHelper
import com.example.codegram.model.ChatMessage
import com.example.codegram.model.User
import com.example.codegram.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(chatHelper: ChatHelper, navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance()
    val focusManager = LocalFocusManager.current
    var message by remember { mutableStateOf("") }
    val senderId = FirebaseAuth.getInstance().currentUser?.uid
    var currentUserAvatar by remember { mutableStateOf<String?>(null) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var receiver by remember { mutableStateOf<User?>(null) }
    var receiverUsername by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // Drawer state
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Fetch current user's avatar
    LaunchedEffect(senderId) {
        senderId?.let {
            val avatar = db.getReference("users").child(it).child("avatar")
            avatar.get().addOnSuccessListener {
                currentUserAvatar = it.getValue(String::class.java)
            }.addOnFailureListener {
                currentUserAvatar = "https://assets.leetcode.com/users/default_avatar.jpg"
            }
        }
    }

    // Fetch receiver's username and messages
    LaunchedEffect(receiver) {
        receiver?.let { rec ->
            db.getReference("users").child(rec.userId).child("username")
                .get()
                .addOnSuccessListener { snapshot ->
                    receiverUsername = snapshot.getValue(String::class.java)
                }
                .addOnFailureListener {
                    receiverUsername = "Unknown"
                }

            senderId?.let {
                val chatId = chatHelper.getChatId(it, rec.userId)
                chatHelper.listenToMessages(chatId) { newMessages ->
                    messages = newMessages.reversed()
                }
            }
        }
    }

    // Automatically scroll to the latest message when messages change
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0F27), Color(0xFF1B1F3A))
                )
            )
    ) {
        ModalNavigationDrawer(
            drawerContent = {
                DrawerContent(
                    onItemClicked = { item ->
                        scope.launch { drawerState.close() }
                        when (item) {
                            "Striver SDE Sheet" -> {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://takeuforward.org/interviews/strivers-sde-sheet-top-coding-interview-problems"))
                                context.startActivity(intent)
                            }
                            "A2Z Sheet" -> {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://takeuforward.org/strivers-a2z-dsa-course/strivers-a2z-dsa-course-sheet-2"))
                                context.startActivity(intent)
                            }
                            "Neetcode 150" -> {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://neetcode.io/practice"))
                                context.startActivity(intent)
                            }
                            "Blind 75" -> {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://takeuforward.org/interviews/blind-75-leetcode-problems-detailed-video-solutions"))
                                context.startActivity(intent)
                            }
                            "Interview Experiences" -> {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://takeuforward.org/interview"))
                                context.startActivity(intent)
                            }
                            "SignOut" -> {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(Screen.Login.route)
                            }
                        }
                    }
                )
            },
            drawerState = drawerState,
            modifier = Modifier.imeNestedScroll()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                    }
            ) {
                // Modern Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1B1F3A),
                                    Color(0xFF2A2F4C),
                                    Color(0xFF3A3F5C)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 0f)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left side - App branding
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // App icon/logo
                            Image(
                                painter = painterResource(R.drawable.launcher),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // App name and tagline
                            Column {
                                Text(
                                    text = "Codesgram",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily(Font(R.font.comforta_bold))
                                )
                                Text(
                                    text = "Connect • Code • Grow",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Right side - Menu button
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (receiver == null) {
                    senderId?.let {
                        UserListScreen(
                            chatHelper = chatHelper,
                            currentUserId = it,
                            currentUserAvatar = currentUserAvatar,
                            onUserClick = { selectedUser -> receiver = selectedUser },
                            navController
                        )
                    }
                } else {
                    // Modern Chat Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1B1F3A),
                                        Color(0xFF2A2F4C),
                                        Color(0xFF3A3F5C)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(1000f, 0f)
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                                spotColor = Color.Black.copy(alpha = 0.3f)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back button
                            IconButton(
                                onClick = { receiver = null },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // User avatar
                            receiver?.avatar?.let { avatarUrl ->
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        .clickable {
                                            navController.navigate(
                                                Screen.LeetCodeStats.createRoute(
                                                    receiverUsername ?: "Unknown"
                                                )
                                            )
                                        }
                                )

                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            // User info
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = receiverUsername ?: "Unknown",
                                    fontFamily = FontFamily(Font(R.font.comforta_bold)),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Tap to view profile",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Messages List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f)
                    ) {
                        items(messages) { message ->
                            PersonalMessageItem(message = message, currentUserId = senderId)
                        }
                    }

                    // Input Row
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2F4C).copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = message,
                                onValueChange = { message = it },
                                label = { Text("Type your message", color = Color.White.copy(alpha = 0.6f)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = LocalTextStyle.current.copy(color = Color.White)
                            )
                            IconButton(
                                onClick = {
                                    senderId?.let {
                                        chatHelper.sendMessage(it, receiver?.userId ?: "", message)
                                    }
                                    message = ""
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        Color(0xFF7AB2D3),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
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
fun PersonalMessageItem(message: ChatMessage, currentUserId: String?) {
    val isSentByCurrentUser = message.senderId == currentUserId
    var senderAvatar by remember { mutableStateOf<String?>(null) }
    var senderName by remember { mutableStateOf("") }

    // Fetch sender's avatar and name from Firebase
    LaunchedEffect(message.senderId) {
        val db = FirebaseDatabase.getInstance().getReference("users").child(message.senderId)
        db.child("avatar").get().addOnSuccessListener {
            senderAvatar = it.getValue(String::class.java)
        }
        db.child("username").get().addOnSuccessListener {
            senderName = it.getValue(String::class.java) ?: "Unknown"
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isSentByCurrentUser) {
            AsyncImage(
                model = senderAvatar,
                contentDescription = "Sender Avatar",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (isSentByCurrentUser) Alignment.End else Alignment.Start) {
            if (!isSentByCurrentUser) {
                Text(
                    text = senderName,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        if (isSentByCurrentUser) Color(0xFF7AB2D3) else Color(0xFF2A2F4C),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
                    .widthIn(max = 250.dp)
            ) {
                Text(
                    text = message.message,
                    color = if (isSentByCurrentUser) Color.White else Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (isSentByCurrentUser) {
            Spacer(Modifier.width(8.dp))
            AsyncImage(
                model = senderAvatar,
                contentDescription = "Your Avatar",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrawerContent(onItemClicked: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Navigation",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        val items = listOf(
            "Striver SDE Sheet",
            "A2Z Sheet",
            "Neetcode 150",
            "Blind 75",
            "Interview Experiences",
            "SignOut"
        )
        items.forEach { item ->
            Text(
                text = item,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClicked(item) }
                    .padding(vertical = 8.dp)
            )
        }
    }
}