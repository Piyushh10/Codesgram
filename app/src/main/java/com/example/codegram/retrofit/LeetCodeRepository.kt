package com.example.codegram.retrofit

import com.example.codegram.model.leetcode.ContestDetails
import com.example.codegram.model.leetcode.UserProfile

class LeetCodeRepository(private val api: LeetCodeApi) {
    suspend fun fetchUserProfile(username: String): UserProfile? {
        return try {
            api.userProfile(username)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Handle the error or return a default value
        }
    }

    suspend fun fetchUserRating(username: String): ContestDetails?{
        return try{
            api.getUserContest(username)
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
}
