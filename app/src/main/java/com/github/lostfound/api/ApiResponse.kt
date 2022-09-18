package com.github.lostfound.api

data class ApiResponse<DataType>(
    var code: Int,
    var data: DataType?,
    var error: String?
) {
    fun checkException(): ApiResponse<DataType> {
        if (code != 0)
            throw RuntimeException(error)
        return this
    }
}