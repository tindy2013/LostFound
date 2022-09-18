package com.github.lostfound.api

import com.github.lostfound.entity.User
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {
    @FormUrlEncoded
    @POST("user/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Result<ApiResponse<User>>

    @GET("user/logout")
    suspend fun logout(): Result<ApiResponse<String>>

    @FormUrlEncoded
    @POST("user/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Result<ApiResponse<String>>

    @FormUrlEncoded
    @POST("user/reset-pass")
    suspend fun resetPassword(
        @Field("old_pass") oldPassword: String,
        @Field("new_pass") newPassword: String
    ): Result<ApiResponse<String>>

    @FormUrlEncoded
    @POST("user/update")
    suspend fun updateInfo(
        @Field("username") username: String? = null,
        @Field("contactName") contactName: String? = null,
        @Field("contactNumber") contactNumber: String? = null
    ): Result<ApiResponse<String>>
}