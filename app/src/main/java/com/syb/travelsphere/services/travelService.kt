package com.syb.travelsphere.services

import android.webkit.WebStorage.Origin
import retrofit2.Response

class TravelService {

    // Static variable to store the selfUserID
    companion object {
        var selfUserID: String? = null
    }

    private val apiService = RetrofitClient.apiService

    // Register User
    suspend fun registerUser(email: String, username: String, password: String, profilePicture: String? = null, originCountry: String, location: Geotag): ApiResponse? {
        val user = User(email, username, password, location, originCountry, profilePicture)
        val response: Response<ApiResponse> = apiService.registerUser(user)
        return if (response.isSuccessful) response.body() else null
    }

    // Login User
    suspend fun loginUser(email: String, password: String): ApiResponse? {
        val credentials = mapOf("email" to email, "password" to password)
        val response: Response<ApiResponse> = apiService.loginUser(credentials)
        return if (response.isSuccessful) {
            // Save selfUserID after successful login
            selfUserID = response.body()?.user?.email // Or any other field, based on what you need
            response.body()
        } else {
            null
        }
    }

    // Create Post
    suspend fun createPost(location: String, description: String, timeOfVisit: String, photos: List<String>, geotag: Geotag): ApiResponse? {
        val userId = selfUserID // Use the static selfUserID here
        val post = Post(null, userId ?: "", location, description, timeOfVisit,photos,geotag, likes = 0 , null)
        val response: Response<ApiResponse> = apiService.createPost(post)
        return if (response.isSuccessful) response.body() else null
    }

    // Get User Posts (Travel Map)
    suspend fun getUserPosts(userId: String = selfUserID ?: ""): List<Post>? {
        val response: Response<List<Post>> = apiService.getUserPosts(userId)
        return if (response.isSuccessful) response.body() else null
    }

    // Get Nearby Posts
    suspend fun getAllPosts(): List<Post>? {
        val response: Response<List<Post>> = apiService.getAllPosts()
        return if (response.isSuccessful) response.body() else null
    }

    // Update Post
    suspend fun updatePost(postId: String, updates: Map<String, Any>): ApiResponse? {
        val response: Response<ApiResponse> = apiService.updatePost(postId, updates)
        return if (response.isSuccessful) response.body() else null
    }

    // Delete Post
    suspend fun deletePost(postId: String): ApiResponse? {
        val response: Response<ApiResponse> = apiService.deletePost(postId)
        return if (response.isSuccessful) response.body() else null
    }

    // Like Post
    suspend fun likePost(postId: String): ApiResponse? {
        val response: Response<ApiResponse> = apiService.likePost(postId)
        return if (response.isSuccessful) response.body() else null
    }

    //  get nearby users
    suspend fun getNearbyUsers(longitude: Double, latitude: Double, radius: Double): List<User>? {
        val response = apiService.getNearbyUsers(longitude, latitude, radius)
        return if (response.isSuccessful) {
            response.body()?.users
        } else {
            null
        }
    }
}
