package com.example.codesgram.model.leetcode

data class Group(
    val name: String,
    val minRating:Double,
    val maxRating: Double
)

val groups = listOf(
    Group("Beginner", 0.0, 1000.0),
    Group("Intermediate", 1001.0, 2000.0),
    Group("Advanced", 2001.0, 3000.0)
)