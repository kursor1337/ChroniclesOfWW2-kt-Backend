package com.kursor.chroniclesofww2.game.entities

import com.kursor.chroniclesofww2.game.GameFeaturesMessages
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


//player 1 always initiator , it is a host of the game
class GameSession(
    val id: Int,
    val initiatorGameData: GameData,
) {

    val initiatorPlayer = initiatorGameData.me
    val connectedPlayer = initiatorGameData.enemy

    val ruleManager = RuleManager(Model(initiatorGameData))

    private var _initiatorClient: Client? = null
    private var _connectedClient: Client? = null

    val initiatorClient: Client
        get() = _initiatorClient!!
    val connectedClient: Client
        get() = _connectedClient!!

    var gameStarted: Boolean = false

    val messageHandler = MessageHandler { login, receiveDTO ->
        val client = getClientWithLogin(login) ?: return@MessageHandler
        val otherClient = getOtherClientForLogin(login) ?: return@MessageHandler
        val type = receiveDTO.type
        val message = receiveDTO.message
        when (type) {
            GameSessionMessageType.MOVE -> {
                val move = Move.decodeFromStringToSimplified(message).restore(ruleManager.model)
                if (!ruleManager.checkMoveForValidity(move)) {
                    client.webSocketSession.send(GameFeaturesMessages.INVALID_MOVE)
                    return@MessageHandler
                }
                otherClient.webSocketSession.send(message)
            }
            GameSessionMessageType.DISCONNECT -> {
                otherClient.webSocketSession.send(GameFeaturesMessages.OTHER_PLAYER_DISCONNECTED)
                listener?.onGameSessionStopped(this)
            }
            else -> Log.i(TAG, "$type:$message")
        }
    }

    var listener: Listener? = null

    fun getPlayerWithName(name: String): Player? = when (name) {
        initiatorPlayer.name -> initiatorPlayer
        connectedPlayer.name -> connectedPlayer
        else -> null
    }

    private fun getClientWithLogin(login: String): Client? = when (login) {
        initiatorClient.player.name -> initiatorClient
        connectedClient.player.name -> connectedClient
        else -> null
    }

    private fun getOtherClientForLogin(login: String): Client? = when (login) {
        initiatorClient.player.name -> connectedClient
        connectedClient.player.name -> initiatorClient
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
        if (player.name == initiatorPlayer.name) {
            _initiatorClient = Client(initiatorPlayer, webSocketServerSession)

        } else {
            _connectedClient = Client(connectedPlayer, webSocketServerSession)
        }

    }

    suspend fun start() {
        if (_connectedClient == null || _initiatorClient == null) return
        initiatorClient.webSocketSession.send(Frame.Text(GameFeaturesMessages.GAME_STARTED))
        connectedClient.webSocketSession.send(Frame.Text(GameFeaturesMessages.GAME_STARTED))
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
    }


    fun interface MessageHandler {
        suspend fun onPlayerMessage(login: String, gameSessionReceiveDTO: GameSessionReceiveDTO)
    }

    interface Listener {
        suspend fun onGameSessionStarted(gameSession: GameSession) {}
        suspend fun onGameSessionStopped(gameSession: GameSession) {}
    }

}


@Serializable
data class GameSessionReceiveDTO(
    val type: GameSessionMessageType,
    val message: String
)

enum class GameSessionMessageType {

    CONNECT, DISCONNECT, MOVE, ERROR

}
