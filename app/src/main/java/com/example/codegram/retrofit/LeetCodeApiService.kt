package com.example.codegram.retrofit

import com.example.codegram.model.leetcode.Badge
import com.example.codegram.model.leetcode.Comment
import com.example.codegram.model.leetcode.ContestDetails
import com.example.codegram.model.leetcode.ContestHistory
import com.example.codegram.model.leetcode.ContestRanking
import com.example.codegram.model.leetcode.DailyProblem
import com.example.codegram.model.leetcode.Discussion
import com.example.codegram.model.leetcode.DiscussionTopic
import com.example.codegram.model.leetcode.LanguageStats
import com.example.codegram.model.leetcode.LeetCodeStats
import com.example.codegram.model.leetcode.QuestionProgress
import com.example.codegram.model.leetcode.SkillStats
import com.example.codegram.model.leetcode.Solved
import com.example.codegram.model.leetcode.Submission
import com.example.codegram.model.leetcode.SubmissionCalendar
import com.example.codegram.model.leetcode.SubmissionResponse
import com.example.codegram.model.leetcode.UserProfile
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LeetCodeApi {
    @GET("/userProfile/{username}")
    suspend fun getProfile(@Path("username") username: String): LeetCodeStats
    // User Profile
    @GET("/{username}")
    suspend fun userProfile(@Path("username") username: String): UserProfile

    @GET("/{username}/badges")
    suspend fun getUserBadges(@Path("username") username: String): List<Badge>

    @GET("/{username}/solved")
    suspend fun getUserSolved(@Path("username") username: String): Solved

    @GET("/{username}/contest")
    suspend fun getUserContest(@Path("username") username: String): ContestDetails

    @GET("/{username}/contest/history")
    suspend fun getUserContestHistory(@Path("username") username: String): List<ContestHistory>

    @GET("/{username}/submission")
    suspend fun getUserLast20Submissions(@Path("username") username: String): SubmissionResponse

    @GET("/{username}/submission")
    suspend fun getUserLimitedSubmissions(
        @Path("username") username: String,
        @Query("limit") limit: Int
    ): List<Submission>

    @GET("/{username}/acSubmission")
    suspend fun getUserAcceptedSubmissions(@Path("username") username: String): List<Submission>

    @GET("/{username}/acSubmission")
    suspend fun getUserLimitedAcceptedSubmissions(
        @Path("username") username: String,
        @Query("limit") limit: Int
    ): List<Submission>

    @GET("/{username}/calendar")
    suspend fun getUserSubmissionCalendar(@Path("username") username: String): SubmissionCalendar

    @GET("/userProfileCalendar")
    suspend fun getUserCalendarForYear(
        @Query("username") username: String,
        @Query("year") year: Int
    ): SubmissionCalendar

    @GET("/languageStats")
    suspend fun getUserLanguageStats(@Query("username") username: String): LanguageStats

    @GET("/userProfileUserQuestionProgressV2/{userSlug}")
    suspend fun getUserQuestionProgress(@Path("userSlug") userSlug: String): QuestionProgress

    @GET("/skillStats/{username}")
    suspend fun getUserSkillStats(@Path("username") username: String): SkillStats

    @GET("/userContestRankingInfo/{username}")
    suspend fun getUserContestRanking(@Path("username") username: String): ContestRanking

    // Discussions
    @GET("/trendingDiscuss")
    suspend fun getTrendingDiscussions(@Query("first") count: Int): List<Discussion>

    @GET("/discussTopic/{topicId}")
    suspend fun getDiscussionTopic(@Path("topicId") topicId: String): DiscussionTopic

    @GET("/discussComments/{topicId}")
    suspend fun getDiscussionComments(@Path("topicId") topicId: String): List<Comment>

    // Problems
    @GET("/dailyQuestion")
    suspend fun getRawDailyProblem(): DailyProblem
}

object ApiService{
    private const val BASE_URL = "https://code-rooms-ayf3o60qv-parmesh-girdonias-projects.vercel.app/"
    private const val TOKEN = "Njk0jzEtudaAiIcZZxgr7uC7"

    private val authInterceptor = Interceptor{chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Auth", "Bearer $TOKEN")
            .build()
        chain.proceed(newRequest)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    // Retrofit instance with OkHttpClient
    val leetCodeApi: LeetCodeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LeetCodeApi::class.java)
    }
}