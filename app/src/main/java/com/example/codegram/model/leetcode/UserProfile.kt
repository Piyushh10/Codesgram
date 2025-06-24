package com.example.codegram.model.leetcode

data class UserProfile(
    val about: String,
    val avatar: String,
    val birthday: String,
    val company: Any,
    val country: String,
    val gitHub: String,
    val linkedIN: String,
    val name: String,
    val ranking: Int,
    val reputation: Int,
    val school: String,
    val skillTags: List<Any>,
    val twitter: Any
)