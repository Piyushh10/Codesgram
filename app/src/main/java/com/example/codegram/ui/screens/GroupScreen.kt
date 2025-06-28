package com.example.codegram.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codegram.R
import com.example.codegram.chat.ChatHelper
import com.example.codegram.model.ChatMessage
import com.example.codegram.model.leetcode.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.navigation.NavController
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.TextButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupChatScreen(group: Group, chatHelper: ChatHelper, navController: NavController) {
    val focusManager = LocalFocusManager.current
    var message by remember { mutableStateOf("") }
    val senderId = FirebaseAuth.getInstance().currentUser?.uid
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    val listState = rememberLazyListState()
    val dbGrp = FirebaseDatabase.getInstance().getReference("groups").child(senderId!!).child("groupName")
    var groupName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val db = FirebaseDatabase.getInstance().getReference("groupMessages").child(groupName).child("senderId")
    var showMembersDialog by remember { mutableStateOf(false) }
    var groupMembers by remember { mutableStateOf<List<Pair<String, String?>>>(emptyList()) } // Pair<username, avatarUrl>

    // Fetch messages for this group and scroll to the latest message when messages change
    LaunchedEffect(senderId) {
        dbGrp.get().addOnSuccessListener { snapshot->
            groupName = snapshot.getValue(String::class.java).toString()?: "Unknown"
            // Add user to group members node if not already present
            if (groupName.isNotBlank() && groupName != "null" && groupName != "Unknown") {
                val membersRef = FirebaseDatabase.getInstance().getReference("groups").child(groupName).child("members").child(senderId)
                membersRef.setValue(true)
            }
        }
        db.get().addOnSuccessListener { snapshot->
            username = snapshot.getValue(String::class.java).toString()
        }
    }

    chatHelper.listenToGroupMessages(groupName) { newMessages ->
        messages = newMessages.reversed()
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1) // Scroll to the most recent message
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
        // Modern Group Chat Header
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
                    onClick = { navController.popBackStack() },
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
                // Group info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (groupName.isBlank() || groupName == "null" || groupName == "Unknown") "Loading..." else groupName,
                        fontFamily = FontFamily(Font(R.font.comforta_bold)),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7AB2D3),
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Group Chat",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                // Group members icon button
                IconButton(
                    onClick = {
                        // Fetch group members from Firebase
                        val groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupName).child("members")
                        groupRef.get().addOnSuccessListener { snapshot ->
                            val memberIds = snapshot.children.mapNotNull { it.key }
                            val usersRef = FirebaseDatabase.getInstance().getReference("users")
                            val membersList = mutableListOf<Pair<String, String?>>()
                            var loaded = 0
                            if (memberIds.isEmpty()) {
                                groupMembers = emptyList()
                                showMembersDialog = true
                            }
                            memberIds.forEach { userId ->
                                usersRef.child(userId).get().addOnSuccessListener { userSnap ->
                                    val username = userSnap.child("username").getValue(String::class.java) ?: "Unknown"
                                    val avatar = userSnap.child("avatar").getValue(String::class.java)
                                    membersList.add(username to avatar)
                                    loaded++
                                    if (loaded == memberIds.size) {
                                        groupMembers = membersList
                                        showMembersDialog = true
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF7AB2D3).copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Group Members",
                        tint = Color(0xFF7AB2D3),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Show group members dialog
        if (showMembersDialog) {
            AlertDialog(
                onDismissRequest = { showMembersDialog = false },
                title = null,
                text = {
                    Column(
                        modifier = Modifier
                            .background(Color(0xFF23274D), RoundedCornerShape(16.dp))
                            .padding(top = 0.dp, bottom = 0.dp, start = 0.dp, end = 0.dp)
                    ) {
                        // Top bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2A2F4C), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF7AB2D3),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Group Members",
                                color = Color(0xFF7AB2D3),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (groupMembers.isEmpty()) {
                            Text(
                                "No members found.",
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                groupMembers.forEach { (username, avatar) ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF2A2F4C).copy(alpha = 0.95f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (avatar != null) {
                                                AsyncImage(
                                                    model = avatar,
                                                    contentDescription = "Avatar",
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape)
                                                        .border(2.dp, Color(0xFF7AB2D3), CircleShape)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = username,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = "Group member",
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMembersDialog = false }) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFF7AB2D3),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Close",
                            color = Color(0xFF7AB2D3),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = Color(0xFF23274D),
                shape = RoundedCornerShape(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Messages list with LazyColumn
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { message ->
                GroupMessageItems(message = message)
            }
        }

        // Modern Message input row
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
                        senderId.let {
                            chatHelper.sendMessageToGroup(groupName, message)
                        }
                        message = "" // Clear the input field
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

@Composable
fun GroupMessageItems(message: ChatMessage) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
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
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (isSentByCurrentUser) Alignment.End else Alignment.Start) {
            if (isCode && codeContent != null) {
                var showDialog by remember { mutableStateOf(false) }
                val preview = codeContent.lines().take(5).joinToString("\n").take(200) + if (codeContent.length > 200 || codeContent.lines().size > 5) "\n..." else ""
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .widthIn(max = 320.dp),
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
                        .widthIn(max = 250.dp)
                ) {
                    Text(
                        text = message.message,
                        color = Color.White,
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
                    .clip(CircleShape)
            )
        }
    }
}