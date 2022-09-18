package com.github.lostfound.entity

import androidx.annotation.ColorInt

data class App(
    val id: Int,
    var icon: Int,
    var name: String,
    @ColorInt var iconTint: Int = -1,
    @ColorInt var nameTint: Int = -1,
    var iconRight: String? = null,
    var textRight: String? = null
)