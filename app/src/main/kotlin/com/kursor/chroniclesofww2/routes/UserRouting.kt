package com.kursor.chroniclesofww2.routes

import com.auth0.jwt.JWT
import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.features.*
import com.kursor.chroniclesofww2.features.RegisterErrorMessages.SUCCESS
import com.kursor.chroniclesofww2.features.RegisterErrorMessages.USER_ALREADY_REGISTERED
import com.kursor.chroniclesofww2.logging.Log
import com.kursor.chroniclesofww2.managers.UserManager
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
        route("/users") {
            get {
                Log.d(TAG, "GET: getting list of users")
                val userInfoResponseList = userManager.getAllUsers().map { UserInfo(it.username) }
                call.respond(userInfoResponseList)
            }

            get("/{login}") {
                val login = call.parameters["login"] ?: return@get
                Log.d(TAG, "GET: $login")
                val user = userManager.getUserByLogin(login = login)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else call.respond(HttpStatusCode.OK, UserInfo.from(user))
            }

            authenticate(AUTH_JWT) {
                put("/change_password") {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString() ?: return@put
                    val changePasswordReceiveDTO = call.receive<ChangePasswordReceiveDTO>()
                    val response = userManager.changePasswordForUser(login, changePasswordReceiveDTO.newPassword)
                    call.respond(response)
                }

                put("/update_userinfo") {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString() ?: return@put
                    val updateUserInfoReceiveDTO = call.receive<UpdateUserInfoReceiveDTO>()
                    val response = userManager.updateUserInfo(login, updateUserInfoReceiveDTO.updatedUserInfo)
                    call.respond(response)
                }
            }

            post("/register") {
                val registerReceiveDTO = call.receive<RegisterReceiveDTO>()
                val respond = userManager.registerUser(registerReceiveDTO)
                val statusCode = when (respond.message) {
                    SUCCESS -> HttpStatusCode.OK
                    USER_ALREADY_REGISTERED -> HttpStatusCode.Conflict
                    else -> HttpStatusCode.BadRequest
                }
                call.respond(statusCode, respond)
            }

            post("/login") {
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
