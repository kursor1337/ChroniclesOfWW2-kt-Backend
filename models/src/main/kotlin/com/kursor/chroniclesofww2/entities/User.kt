package com.kursor.chroniclesofww2.entities

data class User(
    val login: String,
    val username: String,
    val passwordHash: String,
)