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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Web
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.TextButton
import com.example.codegram.ui.theme.dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    chatHelper: ChatHelper,
    navController: NavController,
    personalChatUserId: String? = null
) {
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

    // If personalChatUserId is provided, fetch that user as receiver
    LaunchedEffect(personalChatUserId) {
        if (personalChatUserId != null) {
            db.getReference("users").child(personalChatUserId).get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    receiver = user
                }
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

    // Handle system back button: if in chat, go back to user list or popBackStack if in personalChatUserId mode
    BackHandler(enabled = receiver != null || personalChatUserId != null) {
        if (personalChatUserId != null) {
            navController.popBackStack()
        } else {
            receiver = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0F27), Color(0xFF1B1F3A))
                )
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        if (receiver == null && personalChatUserId == null) {
            // User list screen: wrap in ModalNavigationDrawer
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
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        },
                        onClose = { scope.launch { drawerState.close() } }
                    )
                },
                drawerState = drawerState,
                modifier = Modifier.imeNestedScroll()
            ) {
                Column {
                    // Codesgram top bar with menu button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF23274D))
                            .border(
                                width = 0.5.dp,
                                color = Color(0xFF7AB2D3),
                                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                Image(
                                    painter = painterResource(R.drawable.launcher),
                                    contentDescription = "App Logo",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Codesgram",
                                        color = Color(0xFF7AB2D3),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
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
                                    .size(40.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.08f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color(0xFF7AB2D3),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                    senderId?.let {
                        UserListScreen(
                            chatHelper = chatHelper,
                            currentUserId = it,
                            currentUserAvatar = currentUserAvatar,
                            onUserClick = { selectedUser -> navController.navigate(com.example.codegram.ui.navigation.Screen.PersonalChat.createRoute(selectedUser.userId)) },
                            navController = navController
                        )
                    }
                }
            }
        } else {
            // Only show the user info header in personal chat, not the Codesgram top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF23274D))
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFF7AB2D3),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = {
                            if (personalChatUserId != null) {
                                navController.popBackStack()
                            } else {
                                receiver = null
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF7AB2D3).copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF7AB2D3),
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
                            color = Color(0xFF7AB2D3),
                            fontSize = 17.sp
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

@Composable
fun PersonalMessageItem(message: ChatMessage, currentUserId: String?) {
    val isSentByCurrentUser = message.senderId == currentUserId
    var senderAvatar by remember { mutableStateOf<String?>(null) }
    var senderName by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val isCode = message.message.trim().lowercase().startsWith("/code")
    val codeContent = if (isCode) message.message.trim().removePrefix("/code").trimStart() else null

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
                    .aspectRatio(1f)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (isSentByCurrentUser) Alignment.End else Alignment.Start) {
            // Username always above message
            Text(
                text = senderName,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            if (isCode && codeContent != null) {
                var showDialog by remember { mutableStateOf(false) }
                val preview = codeContent.lines().take(5).joinToString("\n").take(200) + if (codeContent.length > 200 || codeContent.lines().size > 5) "\n..." else ""
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .widthIn(max = 300.dp), // Increased width for better use of space
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF23274D)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF7AB2D3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box {
                        Column(modifier = Modifier.padding(top = 0.dp, bottom = 8.dp, start = 0.dp, end = 0.dp)) {
                            // Top bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2A2F4C), RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = Color(0xFF7AB2D3),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Code",
                                    color = Color(0xFF7AB2D3),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { clipboardManager.setText(AnnotatedString(codeContent)) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy code",
                                        tint = Color(0xFF7AB2D3),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            // Code preview (with clickable overlay)
                            Box {
                                Text(
                                    text = preview,
                                    color = Color(0xFFc9d1d9),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .fillMaxWidth()
                                )
                                Box(
                                    Modifier
                                        .matchParentSize()
                                        .clickable { showDialog = true }
                                )
                            }
                        }
                    }
                }
                if (showDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text(
                                    "Close",
                                    color = Color(0xFF7AB2D3),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        title = { Text("Full Code", color = Color(0xFF7AB2D3), fontWeight = FontWeight.Bold) },
                        text = {
                            Box(modifier = Modifier.heightIn(min = 100.dp, max = 400.dp).verticalScroll(rememberScrollState())) {
                                Text(
                                    text = codeContent,
                                    color = Color(0xFFc9d1d9),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                clipboardManager.setText(AnnotatedString(codeContent))
                            }) {
                                Text(
                                    "Copy All",
                                    color = Color(0xFF7AB2D3),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        containerColor = Color(0xFF23274D)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(
                            if (isSentByCurrentUser) Color(0xFF7AB2D3) else Color(0xFF2A2F4C),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                        .widthIn(max = 300.dp) // Increased width for better use of space
                ) {
                    Text(
                        text = message.message,
                        color = if (isSentByCurrentUser) Color.White else Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        if (isSentByCurrentUser) {
            Spacer(Modifier.width(8.dp))
            AsyncImage(
                model = senderAvatar,
                contentDescription = "Your Avatar",
                modifier = Modifier
                    .size(36.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrawerContent(onItemClicked: (String) -> Unit, onClose: (() -> Unit)? = null) {
    val items = listOf(
        Triple("Striver SDE Sheet", Icons.Default.Star, "Top coding interview problems"),
        Triple("A2Z Sheet", Icons.Default.MenuBook, "A2Z DSA Course"),
        Triple("Neetcode 150", Icons.Default.List, "Neetcode practice list"),
        Triple("Blind 75", Icons.Default.Web, "Blind 75 LeetCode"),
        Triple("Interview Experiences", Icons.Default.Person, "Real interview stories")
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xEE181B2C), Color(0xCC23274D))
                )
            )
            .padding(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.dimens.medium2)
        ) {
            // Top Row: Title and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resources",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (onClose != null) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium2))
            items.forEach { (item, icon, subtitle) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onItemClicked(item) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF23274D).copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item,
                            tint = Color(0xFF7AB2D3),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (subtitle.isNotBlank()) {
                                Text(
                                    text = subtitle,
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Go",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            // Sign Out button at the bottom (improved only this part)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clickable { onItemClicked("SignOut") },
                colors = CardDefaults.cardColors(containerColor = Color(0x22EB5757)), // Slightly red-tinted background
                border = BorderStroke(1.5.dp, Color(0xFFEB5757)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFEB5757),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign Out",
                        color = Color(0xFFEB5757),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}