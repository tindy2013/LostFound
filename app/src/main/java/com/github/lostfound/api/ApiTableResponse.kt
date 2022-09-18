package com.github.lostfound.api

data class ApiTableResponse<ElemType>(
    var code: Int,
    var count: Int,
    var msg: String,
    var data: List<ElemType>
)
