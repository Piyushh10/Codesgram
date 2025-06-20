package com.example.codesgram.model.leetcode

data class ContestDetails(
    val contestAttend: Int,
    val contestBadges: Any,
    val contestGlobalRanking: Int,
    val contestParticipation: List<ContestParticipation>,
    val contestRating: Double,
    val contestTopPercentage: Double,
    val totalParticipants: Int
)