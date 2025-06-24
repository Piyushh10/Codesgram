package com.example.codegram.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codegram.model.leetcode.ContestDetails
import com.example.codegram.model.leetcode.LeetCodeStats
import com.example.codegram.model.leetcode.Submission
import com.example.codegram.retrofit.ApiService
import kotlinx.coroutines.launch

class LeetCodeStatsViewModel : ViewModel() {
    private val _leetcodeStats = MutableLiveData<LeetCodeStats>()
    val leetCodeStats: LiveData<LeetCodeStats> = _leetcodeStats

    private val _contestRating = MutableLiveData<ContestDetails>()
    val contestRating: LiveData<ContestDetails> = _contestRating

    private val _recentSubmissions = MutableLiveData<List<Submission>>()
    val recentSubmissions: LiveData<List<Submission>> = _recentSubmissions

    fun fetchLeetCodeStats(username: String){
        viewModelScope.launch {
            try{
                val stats = ApiService.leetCodeApi.getProfile(username)
                _leetcodeStats.value = stats
                Log.d("LeetCodeStats", "Stats received: $stats")
            }catch(e: Exception){
                Log.e("LeetCodeStats", "Error fetching stats", e)
            }
        }
    }

    fun fetchRating(username: String){
        viewModelScope.launch {
            try{
                val rating = ApiService.leetCodeApi.getUserContest(username)
                _contestRating.value = rating
            }catch (e: Exception){
                Log.e("LeetcodeStats", "Error", e)
            }
        }
    }

    fun fetchRecentSubmissions(username: String, limit: Int = 5) {
        viewModelScope.launch {
            try {
                val response = ApiService.leetCodeApi.getUserLast20Submissions(username)
                _recentSubmissions.value = response.submission
                Log.d("RecentSubmissions", "Submissions: $response.submission")
            } catch (e: Exception) {
                Log.e("LeetCodeStats", "Error fetching recent submissions", e)
            }
        }
    }
}