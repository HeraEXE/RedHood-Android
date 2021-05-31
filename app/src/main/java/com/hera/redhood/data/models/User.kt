package com.hera.redhood.data.models

data class User(
    val idKey: String? = null,
    val email: String? = null,
    val username: String? = null,
    val profileImgUrl: String? = null,
    var subscribers: Int? = null,
    var totalLikes: Int? = null,
    val subscriptions: MutableList<String>? = null,
    val likedHoods: MutableList<String>? = null
)