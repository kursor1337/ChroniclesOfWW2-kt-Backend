package com.kursor.chroniclesofww2.routes

import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.features.DeleteBattleReceiveDTO
import com.kursor.chroniclesofww2.features.EditBattleReceiveDTO
import com.kursor.chroniclesofww2.features.Routes
import com.kursor.chroniclesofww2.features.SaveBattleReceiveDTO
import com.kursor.chroniclesofww2.managers.BattleManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.battleRouting(battleManager: BattleManager) {
    routing {
            authenticate(AUTH_JWT) {
                get(Routes.Battles.GET_ALL.relativePath) {
                    call.respond(battleManager.getAllBattles().map { it.toBattle() })
                }

                get("${Routes.Battles.GET_ALL.relativePath}/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: return@get
                    val battle = battleManager.getBattleById(id)
                    if (battle == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(HttpStatusCode.OK, battle)
                }

                get(Routes.Battles.MY.relativePath) {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString() ?: return@get
                    call.respond(battleManager.getBattlesOfUser(login).map { it.toBattle() })
                }

                post(Routes.Battles.SAVE.relativePath) {
                    val saveBattleReceiveDTO = call.receive<SaveBattleReceiveDTO>()
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString() ?: return@post
                    val response = battleManager.saveBattle(login, saveBattleReceiveDTO)
                    call.respond(response)
                }

                put(Routes.Battles.UPDATE.relativePath) {
                    val editBattleReceiveDTO = call.receive<EditBattleReceiveDTO>()
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString() ?: return@put
                    val response = battleManager.editBattle(login, editBattleReceiveDTO)
                    call.respond(response)
                }

                delete(Routes.Battles.DELETE.relativePath) {
                    val deleteBattleReceiveDTO = call.receive<DeleteBattleReceiveDTO>()
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString() ?: return@delete
                    val response = battleManager.deleteBattle(login, deleteBattleReceiveDTO)
                    call.respond(response)
                }
            }
    }
}