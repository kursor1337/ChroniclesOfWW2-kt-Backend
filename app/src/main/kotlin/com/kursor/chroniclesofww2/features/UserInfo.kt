package com.kursor.chroniclesofww2.features

import com.kursor.chroniclesofww2.entities.User
import kotlinx.serialization.Serializable


@Serializable
data class UserInfo(
    val username: String
) {
    companion object {
        fun from(user: User): UserInfo = UserInfo(user.username)
    }
}

@Serializable
data class ChangePasswordReceiveDTO(
    val token: String,
    val newPassword: String
)

@Serializable
data class UpdateUserInfoReceiveDTO(
    val token: String,
    val updatedUserInfo: UserInfo
)
