package com.kursor.chroniclesofww2.managers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kursor.chroniclesofww2.Variables.JWT_SECRET
import com.kursor.chroniclesofww2.entities.User
import java.util.*

object TokenManager {

    const val TOKEN_LIFETIME = 3_600_000L

    private fun getExpirationDate(): Date = Date(System.currentTimeMillis() + TOKEN_LIFETIME)

    fun generateToken(user: User): String = generateToken(user)

    fun generateToken(login: String): String = JWT.create().withClaim(
        "login", login).withExpiresAt(getExpirationDate()).sign(Algorithm.HMAC256(JWT_SECRET))


}