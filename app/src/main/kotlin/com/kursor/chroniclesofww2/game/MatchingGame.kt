package com.kursor.chroniclesofww2.game

import com.kursor.chroniclesofww2.features.GameFeaturesMessages
import com.kursor.chroniclesofww2.features.MatchingGameMessageDTO
import com.kursor.chroniclesofww2.features.MatchingGameMessageType
import com.kursor.chroniclesofww2.features.MatchingUserInfoDTO
import com.kursor.chroniclesofww2.game.MatchingGame.MessageHandler
import com.kursor.chroniclesofww2.model.game.Nation
import com.kursor.chroniclesofww2.model.game.board.Division
import com.kursor.chroniclesofww2.model.serializable.Battle
import com.kursor.chroniclesofww2.model.serializable.GameData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val MATCHING_GAME_BATTLE = Battle(
    id = -1,
    name = "Match",
    description = "",
    nation1 = Nation.DEFAULT,
    nation1divisions = mapOf(
        Division.Type.INFANTRY to 12,
        Division.Type.ARMORED to 4,
        Division.Type.ARTILLERY to 4
    ),
    nation2 = Nation.DEFAULT,
    nation2divisions = mapOf(
        Division.Type.INFANTRY to 12,
        Division.Type.ARMORED to 4,
        Division.Type.ARTILLERY to 4
    )
)

const val MATCHING_GAME_BOARD_WIDTH = 10
const val MATCHING_GAME_BOARD_HEIGHT = 10

class MatchingGame(
    val id: Int,
    val initiator: MatchingUser,
    val connected: MatchingUser,
    val onStop: suspend (MatchingGame) -> Unit = {},
    val startSession: suspend (MatchingGame) -> Unit = {}
) {

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    var initiatorAgreed = false
    var connectedAgreed = false

    val gameData by lazy {
        GameData(
            myName = initiator.login,
            enemyName = connected.login,
            battle = MATCHING_GAME_BATTLE,
            boardWidth = MATCHING_GAME_BOARD_WIDTH,
            boardHeight = MATCHING_GAME_BOARD_HEIGHT,
            invertNations = false,
            meInitiator = true
        )
    }

    val messageHandler = MessageHandler { login, message ->
        when (message) {
            GameFeaturesMessages.ACCEPTED -> sendGameCondition(login)
            GameFeaturesMessages.REJECTED, GameFeaturesMessages.CANCEL_CONNECTION -> rejectGame()
        }
    }

    init {
        startTimeoutTimer()
        coroutineScope.launch {
            sendMessageInitialized()
        }
    }

    private fun startTimeoutTimer() {
        coroutineScope.launch {
            delay(TIMEOUT)
            val timeoutMessageDTO = MatchingGameMessageDTO(
                type = MatchingGameMessageType.TIMEOUT,
                message = "Timeout"
            )
            sendToMatchingUser(initiator, timeoutMessageDTO)
            sendToMatchingUser(connected, timeoutMessageDTO)
            onStop(this@MatchingGame)
        }
    }

    private suspend fun sendMessageInitialized() {
        sendToMatchingUser(
            initiator,
            MatchingGameMessageDTO(
                type = MatchingGameMessageType.INIT,
                message = Json.encodeToString(
                    MatchingUserInfoDTO(
                        login = connected.login,
                        score = connected.score
                    )
                )
            )
        )
        sendToMatchingUser(
            connected,
            MatchingGameMessageDTO(
                type = MatchingGameMessageType.INIT,
                message = Json.encodeToString(
                    MatchingUserInfoDTO(
                        login = initiator.login,
                        score = initiator.score
                    )
                )
            )
        )

        val gameIdMessageDTO = MatchingGameMessageDTO(
            type = MatchingGameMessageType.GAME_ID,
            message = id.toString()
        )
        sendToMatchingUser(
            initiator,
            gameIdMessageDTO
        )
        sendToMatchingUser(
            connected,
            gameIdMessageDTO
        )

    }

    private suspend fun sendToMatchingUser(
        matchingUser: MatchingUser,
        matchingGameMessageDTO: MatchingGameMessageDTO
    ) {
        matchingUser.client.send(
            Json.encodeToString(
                matchingGameMessageDTO
            )
        )
    }

    private suspend fun rejectGame() {
        val rejectedMessageDTO = MatchingGameMessageDTO(
            type = MatchingGameMessageType.REJECT,
            message = "Rejected"
        )
        sendToMatchingUser(initiator, rejectedMessageDTO)
        sendToMatchingUser(connected, rejectedMessageDTO)
        onStop(this)
    }

    private suspend fun sendGameCondition(login: String) {
        if (initiator.login == login) initiatorAgreed = true
        if (connected.login == login) connectedAgreed = true
        if (initiatorAgreed && connectedAgreed) {
            sendToMatchingUser(
                initiator, MatchingGameMessageDTO(
                    type = MatchingGameMessageType.GAME_DATA,
                    message = Json.encodeToString(gameData)
                )
            )
            sendToMatchingUser(
                connected, MatchingGameMessageDTO(
                    type = MatchingGameMessageType.GAME_DATA,
                    message = Json.encodeToString(gameData.getVersionForAnotherPlayer())
                )
            )
            startSession(this)
        }
    }

    fun interface MessageHandler {
        suspend fun onMessage(login: String, message: String)
    }

    companion object {
        const val TIMEOUT = 15000L
        const val TIMEOUT_MESSAGE = "Timeout"
    }

}