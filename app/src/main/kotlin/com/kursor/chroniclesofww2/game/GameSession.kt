package com.kursor.chroniclesofww2.game

import com.kursor.chroniclesofww2.features.GameFeaturesMessages
import com.kursor.chroniclesofww2.features.GameFeaturesMessages.INVALID_MOVE
import com.kursor.chroniclesofww2.features.GameSessionDTO
import com.kursor.chroniclesofww2.features.GameSessionMessageType
import com.kursor.chroniclesofww2.game.GameSession.MessageHandler
import com.kursor.chroniclesofww2.logging.Log
import com.kursor.chroniclesofww2.model.game.Model
import com.kursor.chroniclesofww2.model.game.RuleManager
import com.kursor.chroniclesofww2.model.game.moves.AddMove
import com.kursor.chroniclesofww2.model.game.moves.MotionMove
import com.kursor.chroniclesofww2.model.game.moves.Move
import com.kursor.chroniclesofww2.model.serializable.GameData
import com.kursor.chroniclesofww2.model.serializable.Player
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


//player 1 always initiator , it is a host of the game
class GameSession(
    val id: Int,
    val initiatorGameData: GameData,
) {

    private val initiatorPlayer = initiatorGameData.me
    private val connectedPlayer = initiatorGameData.enemy

    private val model = Model(initiatorGameData)
    private val ruleManager = RuleManager(model)

    private var initiatorClient: Client? = null
    private var connectedClient: Client? = null


    val clientsInitialized: Boolean
        get() = initiatorClient != null && connectedClient != null

    var gameStarted: Boolean = false

    val messageHandler = MessageHandler { login, receiveDTO ->
        val client = getClientWithLogin(login) ?: return@MessageHandler
        val otherClient = getOtherClientForLogin(login) ?: return@MessageHandler
        val type = receiveDTO.type
        val message = receiveDTO.message
        when (type) {
            GameSessionMessageType.MOVE -> {
                Log.d("GameSession", "$receiveDTO")
                val move = Move.decodeFromStringToSimplified(message).restore(ruleManager.model)
                if (!ruleManager.checkMoveForValidity(move)) {
                    client.send(
                        gameSessionDTO = GameSessionDTO(
                            type = GameSessionMessageType.ERROR,
                            message = INVALID_MOVE
                        )
                    )
                    return@MessageHandler
                }
                otherClient.send(receiveDTO)
                when (move.type) {
                    Move.Type.ADD -> model.handleAddMove(move as AddMove)
                    Move.Type.MOTION -> model.handleMotionMove(move as MotionMove)
                }
                ruleManager.nextTurn()
            }
            GameSessionMessageType.DISCONNECT -> {
                otherClient.send(
                    GameSessionDTO(
                        type = GameSessionMessageType.DISCONNECT,
                        GameFeaturesMessages.OTHER_PLAYER_DISCONNECTED
                    )
                )
                stopSession()
            }
            else -> Log.i(TAG, "$type:$message")
        }
    }

    var listener: Listener? = null


    suspend fun stopSession() {
        initiatorClient?.send(
            GameSessionDTO(
                type = GameSessionMessageType.ERROR,
                message = "stopped session"
            )
        )
        connectedClient?.send(
            GameSessionDTO(
                type = GameSessionMessageType.ERROR,
                message = "stop session"
            )
        )
        initiatorClient?.webSocketSession?.close()
        connectedClient?.webSocketSession?.close()
        listener?.onGameSessionStopped(this)
    }

    suspend fun startTimeoutTimer() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(TIMEOUT)
            if (!clientsInitialized) {
                stopSession()
            }
        }

    }

    fun getPlayerWithName(name: String): Player? = when (name) {
        initiatorPlayer.name -> initiatorPlayer
        connectedPlayer.name -> connectedPlayer
        else -> null
    }

    private fun getClientWithLogin(login: String): Client? = when (login) {
        initiatorClient?.login -> initiatorClient
        connectedClient?.login -> connectedClient
        else -> null
    }

    private fun getOtherClientForLogin(login: String): Client? = when (login) {
        initiatorClient?.login -> connectedClient
        connectedClient?.login -> initiatorClient
        else -> null
    }

    suspend fun initClient(login: String, webSocketServerSession: DefaultWebSocketServerSession) {
        val player = getPlayerWithName(login)
        if (player == null) {
            webSocketServerSession.close(
                CloseReason(
                    CloseReason.Codes.CANNOT_ACCEPT,
                    GameFeaturesMessages.NO_SUCH_PLAYER
                )
            )
            return
        }

        setClient(Client(player.name, webSocketServerSession))
        start()

    }

    fun setClient(client: Client) {
        when {
            client.login != initiatorClient?.login -> initiatorClient = client
            client.login != connectedClient?.login -> connectedClient = client
        }
    }

    suspend fun start() {
        if (!clientsInitialized) return
        initiatorClient!!.send(
            GameSessionDTO(
                type = GameSessionMessageType.GAME_EVENT,
                message = GameFeaturesMessages.GAME_STARTED
            )
        )
        connectedClient!!.send(
            GameSessionDTO(
                type = GameSessionMessageType.GAME_EVENT,
                message = GameFeaturesMessages.GAME_STARTED
            )
        )
        listener?.onGameSessionStarted(this)
        gameStarted = true
    }


    private fun Move.Simplified.restore(model: Model): Move {
        val move = when (type) {
            Move.Type.MOTION -> {
                this as MotionMove.Simplified
                val startRow = startCoordinate / 10
                val startColumn = startCoordinate % 10
                val destRow = destinationCoordinate / 10
                val destColumn = destinationCoordinate % 10
                MotionMove(model.board[startRow, startColumn], model.board[destRow, destColumn])
            }
            Move.Type.ADD -> {
                this as AddMove.Simplified
                val destRow = destinationCoordinate / 10
                val destColumn = destinationCoordinate % 10
                AddMove(
                    model.enemy.divisionResources.reserves[divisionType]!!,
                    model.board[destRow, destColumn]
                )
            }
        }
        return move
    }

    companion object {
        const val TAG = "GameSession"
        const val TIMEOUT = 60000L
    }


    fun interface MessageHandler {
        suspend fun onPlayerMessage(login: String, gameSessionDTO: GameSessionDTO)
    }

    interface Listener {
        suspend fun onGameSessionStarted(gameSession: GameSession) {}
        suspend fun onGameSessionStopped(gameSession: GameSession) {}
    }

}