package com.syb.travelsphere.services
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Response
import javax.security.auth.callback.PasswordCallback

// Data Models
data class User(
    val email: String,
    val password: String?,
    val username: String,
    val location: Geotag,
    val originCountry: String,
    val profilePicture: String?)

data class Geotag(
    val type: String,
    val coordinates: List<Double>
)

data class Post(
    val _id: String?,
    val userId: String,
    val location: String,
    val description: String,
    val timeOfVisit: String,
    val photos: List<String>,
    val geotag: Geotag,  // Accept the object format directly
    val likes: Int,
    val username: String?,
    )

// API Service Interface
interface ApiService {

    @POST("/register")
    suspend fun registerUser(@Body user: User): Response<ApiResponse>

    @POST("/login")
    suspend fun loginUser(@Body request: Map<String, String>): Response<ApiResponse>

    @GET("/users/{userId}/map")
    suspend fun getUserPosts(@Path("userId") userId: String): Response<List<Post>>

    @GET("/posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @POST("/posts")
    suspend fun createPost(@Body post: Post): Response<ApiResponse>

    @PUT("/posts/{postId}")
    suspend fun updatePost(@Path("postId") postId: String, @Body updates: Map<String, Any>): Response<ApiResponse>

    @DELETE("/posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: String): Response<ApiResponse>

    @POST("/posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: String): Response<ApiResponse>

    @GET("/users/nearby")
    suspend fun getNearbyUsers(
        @Query("longitude") longitude: Double,
        @Query("latitude") latitude: Double,
        @Query("radius") radius: Double
    ): Response<ApiResponse>
}

// Retrofit Client
object RetrofitClient {
    private const val BASE_URL = "http://10.100.102.66:5900"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// ApiResponse Data Class
data class ApiResponse(val message: String, val users: List<User>? = null, val user: User? = null, val post: Post? = null)
