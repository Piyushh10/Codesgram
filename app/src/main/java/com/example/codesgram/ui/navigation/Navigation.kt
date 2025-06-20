package com.example.codesgram.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("LoginScreen")
    object SignUp : Screen("SignUpScreen")
    object UserProfile : Screen("UserProfileScreen/{username}") {
        fun createRoute(username: String) = "UserProfileScreen/$username"
    }
    object LeetCodeStats : Screen("LeetCodeStatsScreen/{username}") {
        fun createRoute(username: String) = "LeetCodeStatsScreen/$username"
    }
    object UserList : Screen("UserListScreen")
    object Chat : Screen("chat")
    object GroupChat : Screen("GroupChatScreen/{groupName}") {
        fun createRoute(groupName: String) = "GroupChatScreen/$groupName"
    }
    object FriendProfile : Screen("FriendProfileScreen/{userId}") {
        fun createRoute(userId: String) = "FriendProfileScreen/$userId"
    }
}




