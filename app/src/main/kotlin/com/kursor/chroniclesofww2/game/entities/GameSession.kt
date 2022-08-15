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

    lateinit var initiatorClient: Client
    lateinit var connectedClient: Client


    fun getPlayerWithName(name: String): Player? = when (name) {
        initiatorPlayer.name -> initiatorPlayer
        connectedPlayer.name -> connectedPlayer
        else -> null
    }


    suspend fun start(login: String, webSocketServerSession: DefaultWebSocketServerSession) {

        val player = getPlayerWithName(login)
        if (player == null) {
            webSocketServerSession.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, GameFeaturesMessages.NO_SUCH_PLAYER))
            return
        }
        val otherClient: Client
        if (player.name == initiatorPlayer.name) {
            initiatorClient = Client(initiatorPlayer, webSocketServerSession)
            otherClient = connectedClient
        }
        else {
            connectedClient = Client(connectedPlayer, webSocketServerSession)
            otherClient = initiatorClient
        }


        for (frame in webSocketServerSession.incoming) {
            if (frame !is Frame.Text) continue
            val text = frame.readText()
            val received = Json.decodeFromString<GameSessionReceiveDTO>(text)
            when (received.type) {
                GameSessionMessageType.MOVE -> {
                    val move = Move.decodeFromStringToSimplified(received.message).restore(ruleManager.model)
                    if (!ruleManager.checkMoveForValidity(move)) {
                        webSocketServerSession.send(Frame.Text(GameFeaturesMessages.INVALID_MOVE))
                        continue
                    }
                    otherClient.webSocketSession.send(Frame.Text(
                        text
                    ))

                }
                else -> {
                    Log.i(TAG, text)
                }
            }
        }
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



}
@Serializable
data class GameSessionReceiveDTO(
    val type: GameSessionMessageType,
    val message: String
)

enum class GameSessionMessageType {

    CONNECT, DISCONNECT, MOVE, ERROR

}
