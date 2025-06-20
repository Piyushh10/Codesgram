package com.example.codesgram.model.leetcode

data class LeetCodeStats(
    val totalSolved: Int,
    val totalSubmission: List<TotalSubmission>,
    val totalQuestions: Int,
    val easySolved: Int,
    val totalEasy: Int,
    val mediumSolved: Int,
    val totalMedium: Int,
    val hardSolved: Int,
    val totalHard: Int,
    val ranking: Int,
    val contributionPoint: Int,
    val reputation: Int,
    val recentSubmission: List<RecentSubmission>,
    val matchedUserStats: MatchedUserStats
)