package com.github.lostfound.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Suppress("unused")
@Parcelize
data class Post(
    var id: Int,
    var owner: Int,
    var name: String,
    var type: PostType,
    var image: String,
    var description: String,
    var contactName: String,
    var contactNumber: String,
    var resolved: Boolean,
    var time: String
): Parcelable
