package com.kursor.chroniclesofww2.features

import com.kursor.chroniclesofww2.entities.User
import kotlinx.serialization.Serializable


@Serializable
data class UserInfoResponse(
    val username: String
) {
    companion object {
        fun from(user: User): UserInfoResponse = UserInfoResponse(user.username)
    }
}
