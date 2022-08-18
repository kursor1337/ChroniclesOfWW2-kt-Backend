package com.kursor.chroniclesofww2.routes

import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.features.*
import com.kursor.chroniclesofww2.features.RegisterErrorMessages.SUCCESS
import com.kursor.chroniclesofww2.features.RegisterErrorMessages.USER_ALREADY_REGISTERED
import com.kursor.chroniclesofww2.logging.Log
import com.kursor.chroniclesofww2.managers.UserManager
import com.kursor.chroniclesofww2.userInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.userRouting(userManager: UserManager) {
    val TAG = "userRouting"
    routing {
        route(Routes.Users.relativePath) {
            get("/${Routes.Users.GET_ALL.node}") {
                Log.d(TAG, "GET: getting list of users")
                val userInfoResponseList = userManager.getAllUsers().map { it.userInfo() }
                call.respond(userInfoResponseList)
            }

            get("/${Routes.Users.GET_ALL}/{login}") {
                val login = call.parameters["login"] ?: return@get
                Log.d(TAG, "GET: $login")
                val user = userManager.getUserByLogin(login = login)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else call.respond(HttpStatusCode.OK, user.userInfo())
            }

            post("/${Routes.Users.REGISTER.node}") {
                val registerReceiveDTO = call.receive<RegisterReceiveDTO>()
                val respond = userManager.registerUser(registerReceiveDTO)
                val statusCode = when (respond.message) {
                    SUCCESS -> HttpStatusCode.OK
                    USER_ALREADY_REGISTERED -> HttpStatusCode.Conflict
                    else -> HttpStatusCode.BadRequest
                }
                call.respond(statusCode, respond)
            }

            post("/${Routes.Users.LOGIN.node}") {
                val loginReceiveDTO = call.receive<LoginReceiveDTO>()
                val respond = userManager.loginUser(loginReceiveDTO)
                val statusCode = when (respond.message) {
                    SUCCESS -> HttpStatusCode.OK
                    LoginErrorMessages.NO_SUCH_USER -> HttpStatusCode.NotFound
                    LoginErrorMessages.INCORRECT_PASSWORD -> HttpStatusCode.Unauthorized
                    else -> HttpStatusCode.BadRequest
                }
                call.respond(statusCode, respond)
            }

        }
    }

}
