package com.kursor.chroniclesofww2.routes

import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.features.ChangePasswordReceiveDTO
import com.kursor.chroniclesofww2.features.Routes
import com.kursor.chroniclesofww2.features.UpdateUserInfoReceiveDTO
import com.kursor.chroniclesofww2.managers.UserManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.accountRouting(userManager: UserManager) {
    routing {
        route(Routes.Account.relativePath) {
            authenticate(AUTH_JWT) {
                put("/${Routes.Account.CHANGE_PASSWORD.node}") {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString() ?: return@put
                    val changePasswordReceiveDTO = call.receive<ChangePasswordReceiveDTO>()
                    val response = userManager.changePasswordForUser(login, changePasswordReceiveDTO.newPassword)
                    call.respond(response)
                }

                put("/${Routes.Account.UPDATE_USER_INFO.node}") {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString() ?: return@put
                    val updateUserInfoReceiveDTO = call.receive<UpdateUserInfoReceiveDTO>()
                    val response = userManager.updateUserInfo(login, updateUserInfoReceiveDTO.updatedUserInfo)
                    call.respond(response)
                }

                post("/${Routes.Account.AUTH.node}") {
                    call.respond(HttpStatusCode.OK, "Authorized")
                }
            }
        }
    }
}