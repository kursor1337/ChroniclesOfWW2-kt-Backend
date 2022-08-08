package com.kursor.chroniclesofww2

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.kursor.chroniclesofww2.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSockets()
        configureSerialization()
        configureSecurity()
        configureRouting()
    }.start(wait = true)
}
