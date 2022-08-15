package com.kursor.chroniclesofww2.routes

import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.features.CreateGameReceiveDTO
import com.kursor.chroniclesofww2.features.GameFeaturesMessages
import com.kursor.chroniclesofww2.features.JoinGameReceiveDTO
import com.kursor.chroniclesofww2.game.GameDataWaiting
import com.kursor.chroniclesofww2.game.GameSession
import com.kursor.chroniclesofww2.game.GameSessionReceiveDTO
import com.kursor.chroniclesofww2.managers.GameManager
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

                webSocket("/waiting_room") {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString()
                    if (login == null) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Not authenticated"))
                        return@webSocket
                    }


                    var received = incoming.receive()
                    if (received !is Frame.Text) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Must've sent valid id"))
                    }
                    val gameId = (received as Frame.Text).readText().toInt()

                    //if there is such game that still waits for connected user then wait
                    val gameDataWaiting = gameManager.getWaitingGameById(gameId)
                    if (gameDataWaiting != null) {
                        if (gameDataWaiting.initiatorLogin != login) {
                            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Not initiator"))
                            return@webSocket
                        }
                        send(Frame.Text(GameFeaturesMessages.WAITING_FOR_CONNECTIONS))
                    }

                    val gamesObserver = object : GameManager.GameControllerObserver {
                        override suspend fun onGameSessionInitialized(gameSession: GameSession) {
                            send(Frame.Text(GameFeaturesMessages.USER_CONNECTED))
                            gameManager.stopObservingGames(this)
                            close()
                        }

                        override suspend fun onWaitingGameCreated(gameDataWaiting: GameDataWaiting) {}
                    }
                    gameManager.startObservingGames(gamesObserver)
                }

                webSocket("/session") {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString()
                    if (login == null) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Not authenticated"))
                        return@webSocket
                    }


                    val received = incoming.receive()
                    if (received !is Frame.Text) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Must've sent valid id"))
                    }
                    val gameId = (received as Frame.Text).readText().toInt()

                    val gameSession = gameManager.getGameSessionById(gameId)
                    val player = gameSession?.getPlayerWithName(login)
                    if (player == null) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, GameFeaturesMessages.NO_GAME_WITH_SUCH_ID))
                        return@webSocket
                    }
                    gameSession.initClient(login, this)

                    for (frame in incoming) {
                        if (frame !is Frame.Text) continue
                        val receiveDTO = Json.decodeFromString<GameSessionReceiveDTO>(frame.readText())
                        gameSession.messageHandler.onPlayerMessage(login, receiveDTO)
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
    }
}