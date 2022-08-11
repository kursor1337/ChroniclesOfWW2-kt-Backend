package com.kursor.chroniclesofww2

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.di.appModule
import com.kursor.chroniclesofww2.di.dataModule
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.kursor.chroniclesofww2.plugins.*
import io.ktor.server.application.*
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin

const val PORT = 8080
const val HOST = "0.0.0.0"

fun main() {
    embeddedServer(Netty, port = PORT, host = HOST) {
        install(Koin) {
            modules(appModule, dataModule)
        }
        val db = get<DB>()
        configureSockets()
        configureSerialization()
        configureSecurity()
        configureRouting()
    }.start(wait = true)
}