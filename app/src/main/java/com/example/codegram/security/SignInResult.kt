package com.example.codegram.security

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)
data class UserData(
    val id: String?,
    val name: String?,
    val profileUrl: String?
)