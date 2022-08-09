package com.kursor.chroniclesofww2.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val login: String,
    val username: String,
    val password: String,
)