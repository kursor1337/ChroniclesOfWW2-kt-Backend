package com.kursor.chroniclesofww2.game

import com.kursor.chroniclesofww2.features.CreateGameReceiveDTO
import com.kursor.chroniclesofww2.features.GameFeaturesMessages
import com.kursor.chroniclesofww2.features.JoinGameResponseDTO
import com.kursor.chroniclesofww2.game.WaitingGame.MessageHandler
import com.kursor.chroniclesofww2.model.serializable.Battle
import com.kursor.chroniclesofww2.model.serializable.GameData
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WaitingGame(
    val id: Int,
    val initiator: Client,
    private val password: String,
    val battle: Battle,
    val boardHeight: Int,
    val boardWidth: Int,
    val invertNations: Boolean,
    val onTimeout: suspend (WaitingGame) -> Unit = {},
    val startSession: suspend (WaitingGame) -> Unit = {}
) {

    constructor(
        id: Int,
        webSocketSession: DefaultWebSocketServerSession,
        createGameReceiveDTO: CreateGameReceiveDTO,
        onTimeout: suspend (WaitingGame) -> Unit,
        startSession: suspend (WaitingGame) -> Unit
    ) : this(
        id,
        initiator = Client(
            createGameReceiveDTO.initiatorLogin,
            webSocketSession
        ),
        createGameReceiveDTO.password,
        createGameReceiveDTO.battle,
        createGameReceiveDTO.boardHeight,
        createGameReceiveDTO.boardWidth,
        createGameReceiveDTO.invertNations,
        onTimeout = onTimeout,
        startSession = startSession
    )

    var connected: Client? = null

    val messageHandler = MessageHandler { login, message ->
        when (message) {
            GameFeaturesMessages.ACCEPTED, GameFeaturesMessages.REJECTED -> {
                verdict(message)
            }
            GameFeaturesMessages.CANCEL_CONNECTION -> stop()
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        startTimeoutTimer()
    }

    private fun startTimeoutTimer() {
        coroutineScope.launch {
            delay(TIMEOUT)
            onTimeout(this@WaitingGame)
        }
    }

    suspend fun connectClient(client: Client) {
        connected = client
        initiator.send("${GameFeaturesMessages.REQUEST_FOR_ACCEPT}${client.login}")
    }

    fun checkPassword(password: String?): Boolean = password == this.password

    private suspend fun verdict(string: String) {
        val gameData = if (string == GameFeaturesMessages.ACCEPTED) GameData(
            myName = initiator.login,
            enemyName = connected!!.login,
            battle = battle,
            boardHeight = boardHeight,
            boardWidth = boardWidth,
            invertNations = invertNations
        ) else null

        connected!!.send(
            Json.encodeToString(
                JoinGameResponseDTO(
                    message = string,
                    gameData = gameData?.getVersionForAnotherPlayer()
                )
            )
        )
        if (gameData == null) return
        initiator.send(Json.encodeToString(gameData))
        startSession(this)
    }

    private suspend fun stop() {
        connected?.send(GameFeaturesMessages.CANCEL_CONNECTION)
        onTimeout(this)
    }

    fun interface MessageHandler {
        suspend fun onMessage(login: String, message: String)
    }

    companion object {
        const val TIMEOUT = 120000L
    }

}