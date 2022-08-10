package com.kursor.chroniclesofww2.features.login

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.configureLoginRouting() {
    routing {
        get("/login") {
            val received = call.receive(LoginReceiveRemote::class)

        }
    }
}