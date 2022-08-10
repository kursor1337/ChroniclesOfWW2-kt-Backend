package com.kursor.chroniclesofww2.routes

import io.ktor.server.routing.*

fun Route.userRouting() {
    route("/users") {
        get {

        }

        get("{id?}") {

        }

        post("/register") {

        }

        post("/login") {

        }


    }
}
