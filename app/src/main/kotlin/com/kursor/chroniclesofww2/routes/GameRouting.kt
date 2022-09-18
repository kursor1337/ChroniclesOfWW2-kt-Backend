package com.kursor.chroniclesofww2.routes

import com.kursor.chroniclesofww2.AUTH_JWT
import com.kursor.chroniclesofww2.features.*
import com.kursor.chroniclesofww2.game.Client
import com.kursor.chroniclesofww2.game.WaitingGame
import com.kursor.chroniclesofww2.logging.Log
import com.kursor.chroniclesofww2.managers.GameManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.gameRouting(gameManager: GameManager) {
    routing {
        authenticate(AUTH_JWT) {
            webSocket(Routes.Game.SESSION.relativePath) {
                Log.i("SessionWebSocket", "open web socket")
                val principal = call.principal<JWTPrincipal>()
                val login = principal?.payload?.getClaim("login")?.asString()
                if (login == null) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Not authenticated"))
                    return@webSocket
                }

                Log.i("SessionWebSocket", login)


                val received = incoming.receive()
                Log.d("SessionWebSocket", "$received")
                if (received !is Frame.Text) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Must've sent valid id"))
                }
                Log.d("SessionWebSocket", (received as Frame.Text).readText())
                val gameId = (received as Frame.Text).readText().toInt()

                val gameSession = gameManager.getGameSessionById(gameId)
                val player = gameSession?.getPlayerWithName(login)
                if (player == null) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, GameFeaturesMessages.NO_GAME_WITH_SUCH_ID))
                    return@webSocket
                }
                gameSession.initClient(login, this)

                for (frame in incoming) {
                    Log.d("SessionWebSocket", "$frame")
                    if (frame !is Frame.Text) continue
                    val receiveDTO = Json.decodeFromString<GameSessionDTO>(frame.readText())
                    gameSession.messageHandler.onPlayerMessage(login, receiveDTO)
                }
            }

            webSocket(Routes.Game.CREATE.relativePath) {
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
                val createGameReceiveDTO =
                    Json.decodeFromString<CreateGameReceiveDTO>((received as Frame.Text).readText())
                val response = gameManager.createGame(this, createGameReceiveDTO)
                send(Json.encodeToString(response))

                val gameId = response.gameId
                val waitingGame = gameManager.getWaitingGameById(gameId)!!

                //if there is such game that still waits for connected user then wait

                val gamesObserver = object : GameManager.GameControllerObserver {
                    override suspend fun onWaitingGameTimedOut(waitingGame: WaitingGame) {
                        send(GameFeaturesMessages.SESSION_TIMED_OUT)
                        gameManager.stopObservingGames(this)
                        close()
                    }
                }
                
                gameManager.startObservingGames(gamesObserver)

                for (frame in incoming) {
                    if (frame !is Frame.Text) continue
                    val string = frame.readText()
                    waitingGame.messageHandler.onMessage(login, string)
                }
            }

            webSocket(Routes.Game.JOIN.relativePath) {
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

                val joinGameReceiveDTO = Json.decodeFromString<JoinGameReceiveDTO>((received as Frame.Text).readText())

                val waitingGame = gameManager.getWaitingGameById(joinGameReceiveDTO.gameId)
                if (waitingGame == null) {
                    send(GameFeaturesMessages.NO_GAME_WITH_SUCH_ID)
                    close()
                    return@webSocket
                }
                if (!waitingGame.checkPassword(joinGameReceiveDTO.password)) {
                    send(GameFeaturesMessages.INVALID_PASSWORD)
                    close()
                    return@webSocket
                }
                waitingGame.connectClient(Client(login, this))
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    waitingGame.messageHandler.onMessage(login, frame.readText() )
                }
            }

            get(Routes.Game.relativePath) {
                call.respond(HttpStatusCode.OK, gameManager.getCurrentWaitingGamesInfo())
            }
        }


    }
}