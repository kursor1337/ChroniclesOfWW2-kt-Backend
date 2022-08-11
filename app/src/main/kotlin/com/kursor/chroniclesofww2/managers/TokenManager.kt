package com.kursor.chroniclesofww2.managers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kursor.chroniclesofww2.Variables.JWT_SECRET
import com.kursor.chroniclesofww2.entities.User
import java.util.*

object TokenManager {

    private const val LIFETIME = 3_600_000

    private fun getExpirationDate(): Date = Date(System.currentTimeMillis() + LIFETIME)

    fun generateToken(user: User): String = JWT.create().withClaim(
        "login", user.login).withExpiresAt(getExpirationDate()).sign(Algorithm.HMAC256(JWT_SECRET))


}