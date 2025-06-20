package com.example.codesgram.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.codesgram.model.leetcode.Badge
import com.example.codesgram.model.leetcode.UserProfile
import com.example.codesgram.retrofit.ApiService

class UserProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _userBadges = MutableLiveData<List<Badge>>()
    val userBadges: LiveData<List<Badge>> = _userBadges

    // Fetch user profile
    fun fetchUserProfile(username: String) {
        viewModelScope.launch {
            try {
                val profile = ApiService.leetCodeApi.userProfile(username)
                _userProfile.value = profile
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error fetching user profile", e)
            }
        }
    }

    // Fetch user badges
    fun fetchUserBadges(username: String) {
        viewModelScope.launch {
            try {
                val badges = ApiService.leetCodeApi.getUserBadges(username)
                _userBadges.value = badges
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
