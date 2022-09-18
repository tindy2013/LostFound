package com.github.lostfound.api

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.POST

interface FileService {
    @POST("file/upload")
    suspend fun upload(@Body parts: MultipartBody): Result<ApiResponse<String>>
}