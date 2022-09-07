package com.kursor.chroniclesofww2.game

import com.kursor.chroniclesofww2.features.CreateGameReceiveDTO
import com.kursor.chroniclesofww2.features.GameFeaturesMessages
import com.kursor.chroniclesofww2.features.JoinGameResponseDTO
import com.kursor.chroniclesofww2.model.serializable.Battle
import com.kursor.chroniclesofww2.model.serializable.GameData
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.ref.ReferenceQueue

class WaitingGame(
    val id: Int,
    val initiator: Client,
    val password: String,
    val battle: Battle,
    val boardHeight: Int,
    val boardWidth: Int,
    val invertNations: Boolean
) {

    constructor(id: Int, webSocketSession: DefaultWebSocketServerSession, createGameReceiveDTO: CreateGameReceiveDTO): this(
        id,
        initiator = Client(
            createGameReceiveDTO.initiatorLogin,
            webSocketSession
        ),
        createGameReceiveDTO.password,
        createGameReceiveDTO.battle,
        createGameReceiveDTO.boardHeight,
        createGameReceiveDTO.boardWidth,
        createGameReceiveDTO.invertNations
    )

    var timeoutListener: TimeoutListener? = null

    var connected: Client? = null

    suspend fun startTimeoutTimer() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(TIMEOUT)
            timeoutListener?.onTimeout(this@WaitingGame)
        }
    }

    suspend fun connectClient(client: Client) {
        connected = client
        initiator.send("${GameFeaturesMessages.REQUEST_FOR_ACCEPT}${client.login}")
    }


    suspend fun verdict(string: String) {
        connected?.send(
            Json.encodeToString(
                JoinGameResponseDTO(
                    message = string,
                    gameData = if (string == GameFeaturesMessages.ACCEPTED) GameData(
                        myName = initiator.login,
                        enemyName = connected!!.login,
                        battle = battle,
                        boardHeight = boardHeight,
                        boardWidth = boardWidth,
                        invertNations = invertNations
                    ).getVersionForAnotherPlayer()
                else null
                )
            )
        )
    }

    suspend fun stop() {
        connected?.send(GameFeaturesMessages.CANCEL_CONNECTION)
        timeoutListener?.onTimeout(this)
    }


    fun interface TimeoutListener {
        suspend fun onTimeout(waitingGame: WaitingGame)
    }

    fun interface MessageHandler {
        fun onMessage(login: String, message: String)
    }

    companion object {
        const val TIMEOUT = 120000L
        const val TIMEOUT_MESSAGE = "Timeout"
    }

}