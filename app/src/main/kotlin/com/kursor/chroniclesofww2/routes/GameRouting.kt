package com.kursor.chroniclesofww2.routes

import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.features.*
import com.kursor.chroniclesofww2.game.WaitingGame
import com.kursor.chroniclesofww2.game.GameSession
import com.kursor.chroniclesofww2.managers.GameManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.gameRouting(gameManager: GameManager) {
    routing {
        route(Routes.Game.relativePath) {

            authenticate(AUTH_JWT) {

                webSocket("/${Routes.Game.SESSION.node}") {
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
                        val receiveDTO = Json.decodeFromString<GameSessionDTO>(frame.readText())
                        gameSession.messageHandler.onPlayerMessage(login, receiveDTO)
                    }
                }

                webSocket ("/${Routes.Game.CREATE.node}") {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString()
                    if (login == null) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Not authenticated"))
                        return@webSocket
                    }
                    val received = incoming.receive()
                    if (received !is Frame.Text) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Must've sent valid string"))
                    }
                    val createGameReceiveDTO = Json.decodeFromString<CreateGameReceiveDTO>((received as Frame.Text).readText())
                    val response = gameManager.createGame(this, createGameReceiveDTO)
                    send(Json.encodeToString(response))

                    val gameId = response.gameId

                    //if there is such game that still waits for connected user then wait
                    val gameDataWaiting = gameManager.getWaitingGameById(gameId)

                    send(Frame.Text(GameFeaturesMessages.WAITING_FOR_CONNECTIONS))


                    val gamesObserver = object : GameManager.GameControllerObserver {
                        override suspend fun onGameSessionInitialized(gameSession: GameSession) {
                            send(GameFeaturesMessages.USER_CONNECTED)
                            gameManager.stopObservingGames(this)
                            close()
                        }

                        override suspend fun onWaitingGameTimedOut(waitingGame: WaitingGame) {
                            send(GameFeaturesMessages.SESSION_TIMED_OUT)
                            gameManager.stopObservingGames(this)
                            close()
                        }
                    }
                    gameManager.startObservingGames(gamesObserver)
                    call.respond(response)
                }

                webSocket ("/${Routes.Game.JOIN.node}") {
                    val principal = call.principal<JWTPrincipal>()
                    val login = principal?.payload?.getClaim("login")?.asString()
                    if (login == null) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Not authenticated"))
                        return@webSocket
                    }

                    val received = incoming.receive()
                    if (received !is Frame.Text) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Must've sent valid string"))
                    }

                    val joinGameReceiveDTO = call.receive<JoinGameReceiveDTO>()
                    val response = gameManager.initGameSession(joinGameReceiveDTO)
                    call.respond(response)
                }

                get {
                    call.respond(HttpStatusCode.OK, gameManager.getCurrentWaitingGamesInfo())
                }
            }


        }
    }
}