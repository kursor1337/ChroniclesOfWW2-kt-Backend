package com.kursor.chroniclesofww2.features

import com.kursor.chroniclesofww2.entities.User
import com.kursor.chroniclesofww2.features.UserInfoMessages.SUCCESS
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
    val newPassword: String
)

@Serializable
data class ChangePasswordResponseDTO(
    val token: String?,
    val message: String = SUCCESS
)

@Serializable
data class UpdateUserInfoReceiveDTO(
    val updatedUserInfo: UserInfo
)

@Serializable
data class UpdateUserInfoResponseDTO(
    val message: String = SUCCESS
)

object UserInfoMessages {
    const val SUCCESS = "Success"
    const val NO_SUCH_USER = LoginErrorMessages.NO_SUCH_USER
}
