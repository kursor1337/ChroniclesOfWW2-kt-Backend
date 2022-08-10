package com.kursor.chroniclesofww2.features

import kotlinx.serialization.Serializable


@Serializable
data class UserInfoResponse(
    val username: String
)
