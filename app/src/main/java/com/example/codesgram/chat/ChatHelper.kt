package com.example.codesgram.chat

import android.content.Context
import android.util.Log
import com.example.codesgram.model.ChatMessage
import com.example.codesgram.model.User
import com.google.firebase.database.*
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ChatHelper(private val db: FirebaseDatabase, val context: Context) {

    private val databaseReference: DatabaseReference = db.reference

    // Send message to Realtime Database
    fun sendMessage(senderId: String, receiverId: String, messageText: String) {
        val chatId = getChatId(senderId, receiverId)
        val message = ChatMessage(
            senderId = senderId,
            receiverId = receiverId,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )
        if(message.message.isEmpty()){
            Toast.makeText(context, "Can't send an empty message", Toast.LENGTH_SHORT).show()
        }else{
            databaseReference.child("chats")
                .child(chatId)
                .child("messages")
                .push()
                .setValue(message)
                .addOnSuccessListener {
                    Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Message Not Sent", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun listenToMessages(chatId: String, onNewMessage: (List<ChatMessage>) -> Unit) {
        val messageRef = FirebaseDatabase.getInstance().getReference("chats")
            .child(chatId)
            .child("messages")

        messageRef.orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull {
                        it.getValue(ChatMessage::class.java)
                    }
                    onNewMessage(messages.reversed())
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun getChatId(senderId: String, receiverId: String): String {
        val ids = listOf(senderId, receiverId).sorted()
        return ids.joinToString("_")
    }

    fun fetchUser(onUsersFetched: (List<User>) -> Unit){
        val usersRef = databaseReference.child("users")
        usersRef.get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                onUsersFetched(users)
            }
            .addOnFailureListener {
                Log.e("ChatHelper", "Error Fetching User", it)
            }
    }

    fun listenToGroupMessages(groupName: String, onMessagesFetched: (List<ChatMessage>) -> Unit) {
        val groupMessageRef = databaseReference.child("groupMessages").child(groupName)

        groupMessageRef.orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                    onMessagesFetched(messages.reversed()) // Newest message at the bottom
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatHelper", "Error listening to group messages", error.toException())
                    Toast.makeText(context, "Failed to load messages", Toast.LENGTH_SHORT).show()
                }
            })
    }


    fun sendMessageToGroup(groupName: String, messageText: String) {
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val message = ChatMessage(
            senderId = senderId,
            receiverId = "null", // No specific receiver in a group chat
            message = messageText,
            timestamp = System.currentTimeMillis()
        )
        if(message.message.isEmpty()){
            Toast.makeText(context, "Can't send an empty message", Toast.LENGTH_SHORT).show()
        }else{
            databaseReference.child("groupMessages")
                .child(groupName)
                .push()
                .setValue(message)
                .addOnSuccessListener {
                    Toast.makeText(context, "Message Sent to Group", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Log.e("ChatHelper", "Failed to send message to group", error)
                    Toast.makeText(context, "Message Not Sent to Group", Toast.LENGTH_SHORT).show()
                }
        }
    }
}


