package com.kursor.chroniclesofww2.features.login

import kotlinx.serialization.Serializable


@Serializable
data class LoginReceiveRemote(
    val login: String,
    val password: String
)

@Serializable
data class LoginResponseRemote(
    val token: String
)