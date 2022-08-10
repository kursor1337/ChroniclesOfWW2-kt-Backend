package com.kursor.chroniclesofww2.routes

import com.kursor.chroniclesofww2.features.LoginErrorMessages
import com.kursor.chroniclesofww2.features.LoginReceiveDTO
import com.kursor.chroniclesofww2.features.RegisterErrorMessages.USER_ALREADY_REGISTERED
import com.kursor.chroniclesofww2.features.RegisterReceiveDTO
import com.kursor.chroniclesofww2.managers.UserManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRouting(userManager: UserManager) {
    route("/users") {
        get {

        }

        get("{id?}") {

        }

        post("/register") {
            val registerReceiveDTO = call.receive<RegisterReceiveDTO>()
            val respond = userManager.registerUser(registerReceiveDTO)
            val statusCode = when (respond.errorMessage) {
                null -> HttpStatusCode.OK
                USER_ALREADY_REGISTERED -> HttpStatusCode.Conflict
                else -> HttpStatusCode.BadRequest
            }
            call.respond(statusCode, respond)
        }

        post("/login") {
            val loginReceiveDTO = call.receive<LoginReceiveDTO>()
            val respond = userManager.loginUser(loginReceiveDTO)
            val statusCode = when (respond.errorMessage) {
                null -> HttpStatusCode.OK
                LoginErrorMessages.NO_SUCH_USER -> HttpStatusCode.NotFound
                LoginErrorMessages.INCORRECT_PASSWORD -> HttpStatusCode.Unauthorized
                else -> HttpStatusCode.BadRequest
            }
            call.respond(statusCode, respond)
        }


    }
}
