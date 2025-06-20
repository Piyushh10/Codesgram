package com.example.codesgram.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codesgram.R
import com.example.codesgram.chat.ChatHelper
import com.example.codesgram.model.ChatMessage
import com.example.codesgram.model.leetcode.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupChatScreen(group: Group, chatHelper: ChatHelper) {
    val focusManager = LocalFocusManager.current
    var message by remember { mutableStateOf("") }
    val senderId = FirebaseAuth.getInstance().currentUser?.uid
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    val listState = rememberLazyListState()
    val dbGrp = FirebaseDatabase.getInstance().getReference("groups").child(senderId!!).child("groupName")
    var groupName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val db = FirebaseDatabase.getInstance().getReference("groupMessages").child(groupName).child("senderId")

    // Fetch messages for this group and scroll to the latest message when messages change
    LaunchedEffect(senderId) {
        dbGrp.get().addOnSuccessListener { snapshot->
            groupName = snapshot.getValue(String::class.java).toString()?: "Unknown"
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
            .background(Color.White)
            .padding(8.dp)
            .padding(top = 20.dp)
            .imePadding()
            .imeNestedScroll()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        // Display the group name
        Text(
            text = groupName,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp),
            fontFamily = FontFamily(Font(R.font.gameday)),
            fontSize = 24.sp
        )

        // Messages list with LazyColumn
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { message ->
                MessageItems(message = message)
            }
        }

        // Message input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Type your message") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topEnd = 20.dp)
            )
            IconButton(
                onClick = {
                    senderId.let {
                        chatHelper.sendMessageToGroup(groupName, message)
                    }
                    message = "" // Clear the input field
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.send),
                    contentDescription = "Send",
                    Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun MessageItems(message: ChatMessage) {
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
        ) {
            Text(
                text = message.message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}