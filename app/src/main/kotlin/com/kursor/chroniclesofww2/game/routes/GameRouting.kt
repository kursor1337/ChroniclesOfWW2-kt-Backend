package com.kursor.chroniclesofww2.game.routes

import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.game.CreateGameReceiveDTO
import com.kursor.chroniclesofww2.game.JoinGameReceiveDTO
import com.kursor.chroniclesofww2.game.WebSocketMessageType
import com.kursor.chroniclesofww2.game.WebSocketReceiveDTO
import com.kursor.chroniclesofww2.game.entities.GameSession
import com.kursor.chroniclesofww2.game.features.WebSocketMessageType.CONNECT
import com.kursor.chroniclesofww2.game.features.WebSocketReceiveDTO
import com.kursor.chroniclesofww2.game.managers.GameManager
import com.kursor.chroniclesofww2.model.serializable.Player
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun Application.gameRouting(gameManager: GameManager) {
    routing {
        route("/game") {

            authenticate(AUTH_JWT) {

                webSocket("/session") {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString()
                    if (login == null) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Not authenticated"))
                        return@webSocket
                    }

                    var gameSession: GameSession? = null
                    var player1: Player? = null
                    var player2: Player? = null

                    for (frame in incoming) {
                        if (frame !is Frame.Text) continue
                        val text = frame.readText()
                        val (type, message) = Json.decodeFromString<WebSocketReceiveDTO>(text)
                        when (type) {
                            WebSocketMessageType.CONNECT -> {
                                val gameId = message.toInt()
                                val gameSession = gameManager.getGameSessionById(gameId)
                                if (gameSession.)
                            }
                        }
                    }

                    webSocket("/join") {

                    }

                }

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

        webSocket() {  }
    }
}