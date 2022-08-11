package com.kursor.chroniclesofww2.features

import com.kursor.chroniclesofww2.features.LoginErrorMessages.SUCCESS
import kotlinx.serialization.Serializable


@Serializable
data class LoginReceiveDTO(
    val login: String,
    val password: String
)

@Serializable
data class LoginResponseDTO(
    val token: String?,
    val message: String = SUCCESS
)

object LoginErrorMessages {
    const val SUCCESS = "Success"
    const val INCORRECT_PASSWORD = "Incorrect Password"
    const val NO_SUCH_USER = "No such user"
}

