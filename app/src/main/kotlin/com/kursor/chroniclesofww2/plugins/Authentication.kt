package com.kursor.chroniclesofww2.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.Variables
import com.kursor.chroniclesofww2.logging.Log
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt(AUTH_JWT) {
            verifier(
                JWT.require(Algorithm.HMAC256(Variables.JWT_SECRET))
                    .build()
            )
            validate { credential ->
                Log.d("Auth JWT", credential.payload.getClaim("login").asString())
                if (credential.payload.getClaim("login").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { m, d ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}