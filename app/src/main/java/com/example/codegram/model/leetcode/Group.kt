package com.example.codegram.model.leetcode

data class Group(
    val name: String,
    val minRating: Int,
    val maxRating: Int,
    val minSolved: Int,
    val maxSolved: Int
)

val groups = listOf(
    Group("Beginner", 0, 1400, 0, 200),
    Group("Intermediate", 1401, 1700, 201, 500),
    Group("Advanced", 1701, Int.MAX_VALUE, 501, Int.MAX_VALUE)
)