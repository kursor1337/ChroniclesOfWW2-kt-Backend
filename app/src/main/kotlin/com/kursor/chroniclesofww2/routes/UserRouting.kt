package com.kursor.chroniclesofww2.routes

import com.kursor.chroniclesofww2.features.*
import com.kursor.chroniclesofww2.features.RegisterErrorMessages.SUCCESS
import com.kursor.chroniclesofww2.features.RegisterErrorMessages.USER_ALREADY_REGISTERED
import com.kursor.chroniclesofww2.logging.Log
import com.kursor.chroniclesofww2.managers.UserManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.userRouting(userManager: UserManager) {
    val TAG = "userRouting"
    routing {
        get(Routes.Users.GET_ALL.relativePath) {
            Log.d(TAG, "GET: getting list of users")
            val userInfoResponseList = userManager.getAllUserInfos()
            call.respond(userInfoResponseList)
        }

        get("${Routes.Users.GET_ALL.relativePath}/{login}") {
            val login = call.parameters["login"] ?: return@get
            Log.d(TAG, "GET: $login")
            val userInfo = userManager.getUserInfoByLogin(login = login)
            if (userInfo == null) {
                call.respond(HttpStatusCode.NotFound)
            } else call.respond(HttpStatusCode.OK, userInfo)
        }

        post(Routes.Users.REGISTER.relativePath) {
            val registerReceiveDTO = call.receive<RegisterReceiveDTO>()
            val respond = userManager.registerUser(registerReceiveDTO)
            val statusCode = when (respond.message) {
                SUCCESS -> HttpStatusCode.OK
                USER_ALREADY_REGISTERED -> HttpStatusCode.Conflict
                else -> HttpStatusCode.BadRequest
            }
            call.respond(statusCode, respond)
        }

        post(Routes.Users.LOGIN.relativePath) {
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

        get(Routes.Users.LEADERBOARD.relativePath) {
            val leaderboardInfoReceiveDTO = call.receive<LeaderboardInfoReceiveDTO>()
            val leaderboardInfoResponseDTO = userManager.leaderboard(leaderboardInfoReceiveDTO)
            call.respond(leaderboardInfoResponseDTO)
        }
    }

}
