package com.hera.redhood.data.models

data class Hood(
        val idKey: String? = null,
        val userKey: String? = null,
        val title: String? = null,
        val content: String? = null,
        val imageUrl: String? = null,
        var likes: Int? = null
)