package com.kursor.chroniclesofww2.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val login: String,
    val email: String,
    val password: String,
    val userInfo: UserInfo
)

@Serializable
data class UserInfo(
    val score: Int
)