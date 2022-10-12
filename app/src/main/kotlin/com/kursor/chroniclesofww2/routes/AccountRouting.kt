package com.kursor.chroniclesofww2.routes

import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.features.*
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
        authenticate(AUTH_JWT) {
            put(Routes.Account.CHANGE_PASSWORD.relativePath) {
                val principal = call.principal<JWTPrincipal>()
                val login = principal?.payload?.getClaim("login")?.asString() ?: return@put
                val changePasswordReceiveDTO = call.receive<ChangePasswordReceiveDTO>()
                val response = userManager.changePasswordForUser(login, changePasswordReceiveDTO)
                call.respond(response)
            }

            put(Routes.Account.UPDATE_USER_INFO.relativePath) {
                val principal = call.principal<JWTPrincipal>()
                val login = principal?.payload?.getClaim("login")?.asString() ?: return@put
                val updateUserInfoReceiveDTO = call.receive<UpdateUserInfoReceiveDTO>()
                val response = userManager.updateUserInfo(login, updateUserInfoReceiveDTO.updatedUserInfo)
                call.respond(response)
            }

            post(Routes.Account.AUTH.relativePath) {
                call.respond(HttpStatusCode.OK, "Authorized")
            }

            delete(Routes.Account.DELETE.relativePath) {
                val principal = call.principal<JWTPrincipal>()
                val login = principal?.payload?.getClaim("login")?.asString() ?: return@delete
                val deleteUserReceiveDTO = call.receive<DeleteUserReceiveDTO>()
                val response = userManager.deleteUser(login, deleteUserReceiveDTO)
                call.respond(response)
            }
            get(Routes.Account.GET_ACCOUNT_INFO.relativePath) {
                val login = call.principal<JWTPrincipal>()!!.payload.getClaim("login").asString()
                val accountInfo = userManager.getAccountInfo(login)!!
                call.respond(accountInfo)
            }
        }
    }
}