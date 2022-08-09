package com.kursor.chroniclesofww2

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.kursor.chroniclesofww2.plugins.*

const val PORT = 8080
const val HOST = "0.0.0.0"

fun main() {
    embeddedServer(Netty, port = PORT, host = HOST) {
        configureSockets()
        configureSerialization()
        configureSecurity()
        configureRouting()
    }.start(wait = true)
}

