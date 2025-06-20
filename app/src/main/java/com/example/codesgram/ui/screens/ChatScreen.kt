package com.example.codesgram.ui.screens

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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.codesgram.R
import com.example.codesgram.chat.ChatHelper
import com.example.codesgram.model.ChatMessage
import com.example.codesgram.model.User
import com.example.codesgram.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

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

    Column(Modifier
        .imeNestedScroll()
        .imePadding()){
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
                            "Rate us" -> { /* Handle Rating */ }
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
                    .background(Color.Black)
                    //NO IME PADDING
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                    }
            ) {
                // Header
                Box {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp)
                            .imePadding()
                    )

                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.padding(top = 36.dp, end = 3.dp)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
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
                    // Chat Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .background(Color.Black)
                    ) {
                        IconButton(onClick = { receiver = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        receiver?.avatar?.let { avatarUrl ->
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.Gray, CircleShape)
                                    .clickable {
                                        navController.navigate(
                                            Screen.LeetCodeStats.createRoute(
                                                receiverUsername ?: "Unknown"
                                            )
                                        )
                                    }
                            )

                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Text(
                            text = "Messages with ${receiverUsername ?: "Unknown"}",
                            fontFamily = FontFamily(Font(R.font.comforta_bold)),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Messages List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f)
                    ) {
                        items(messages) { message ->
                            MessageItem(message = message)
                        }
                    }

                    // Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color.Black),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Type your message", color = Color.Gray) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        IconButton(
                            onClick = {
                                senderId?.let {
                                    chatHelper.sendMessage(it, receiver?.userId ?: "", message)
                                }
                                message = ""
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isSentByCurrentUser = message.senderId == currentUserId
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isSentByCurrentUser) Color(0xFF7AB2D3) else Color(0xFFCCCCCC),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
                .widthIn(max = 250.dp)
        )
        Text(
            text = message.message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
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
            "Rate us",
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