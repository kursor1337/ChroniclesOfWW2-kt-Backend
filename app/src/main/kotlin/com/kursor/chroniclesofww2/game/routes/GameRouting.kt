package com.kursor.chroniclesofww2.game.routes

import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.game.CreateGameReceiveDTO
import com.kursor.chroniclesofww2.game.JoinGameReceiveDTO
import com.kursor.chroniclesofww2.game.managers.GameManager
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.gameRouting(gameManager: GameManager) {
    routing {
        route("/game") {

            authenticate(AUTH_JWT) {
                post("/create") {
                    val createGameReceiveDTO = call.receive<CreateGameReceiveDTO>()
                    val response = gameManager.createGame(createGameReceiveDTO)
                    call.respond(response)
                }

                post("/join") {
                    val joinGameReceiveDTO = call.receive<JoinGameReceiveDTO>()
                    val response = gameManager.initGameSession(joinGameReceiveDTO)
                    call.respond(response)
                }
            }
        }
    }
}