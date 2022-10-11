package com.kursor.chroniclesofww2.entities

import kotlinx.serialization.Serializable

@Serializable
data class UserScore(
    val login: String,
    val score: Int
)