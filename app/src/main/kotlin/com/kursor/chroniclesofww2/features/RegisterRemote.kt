package com.kursor.chroniclesofww2.features

import com.kursor.chroniclesofww2.features.RegisterErrorMessages.SUCCESS
import kotlinx.serialization.Serializable

@Serializable
data class RegisterReceiveDTO(
    val login: String,
    val username: String,
    val password: String
)

@Serializable
data class RegisterResponseDTO(
    val token: String?,
    val message: String = SUCCESS
)

object RegisterErrorMessages {
    const val SUCCESS = "Success"
    const val USER_ALREADY_REGISTERED = "User already registered"
}

