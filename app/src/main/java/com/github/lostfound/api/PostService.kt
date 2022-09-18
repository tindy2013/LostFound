package com.github.lostfound.api

import com.github.lostfound.entity.Post
import retrofit2.http.*

interface PostService {
    @GET("post/list")
    suspend fun listPosts(
        @Query("self") self: Boolean? = null,
        @Query("keyword") keyword: String? = null,
        @Query("type") type: Int? = null,
        @Query("resolved") resolved: Boolean? = null,
        @Query("page") page: Int,
        @Query("count") count: Int
    ): Result<ApiTableResponse<Post>>

    @FormUrlEncoded
    @POST("post/create")
    suspend fun createPost(
        @Field("name") name: String,
        @Field("desc") desc: String,
        @Field("type") type: Int,
        @Field("image") image: String
    ): Result<ApiResponse<String>>

    @FormUrlEncoded
    @POST("post/update")
    suspend fun updatePost(
        @Field("id") id: Int,
        @Field("name") name: String,
        @Field("desc") desc: String,
        @Field("type") type: Int,
        @Field("image") image: String
    ): Result<ApiResponse<Post>>

    @FormUrlEncoded
    @POST("post/resolve")
    suspend fun resolvePost(
        @Field("id") id: Int,
        @Field("resolved") resolved: Boolean
    ): Result<ApiResponse<Post>>
}