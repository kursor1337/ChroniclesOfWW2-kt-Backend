package com.kursor.chroniclesofww2.features

import kotlinx.serialization.Serializable


@Serializable
data class LoginReceiveDTO(
    val login: String,
    val password: String
)

@Serializable
data class LoginResponseDTO(
    val token: String?,
    val errorMessage: String? = null
)

object LoginErrorMessages {
    const val INCORRECT_PASSWORD = "Incorrect Password"
    const val NO_SUCH_USER = "No such user"
}

