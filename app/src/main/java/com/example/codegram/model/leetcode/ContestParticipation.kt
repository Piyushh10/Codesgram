package com.example.codegram.model.leetcode

data class ContestParticipation(
    val attended: Boolean,
    val contest: Contest,
    val finishTimeInSeconds: Int,
    val problemsSolved: Int,
    val ranking: Int,
    val rating: Double,
    val totalProblems: Int,
    val trendDirection: String
)