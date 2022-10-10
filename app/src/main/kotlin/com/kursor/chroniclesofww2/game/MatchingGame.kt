package com.kursor.chroniclesofww2.game

import com.kursor.chroniclesofww2.features.GameFeaturesMessages
import com.kursor.chroniclesofww2.features.MatchingGameMessageDTO
import com.kursor.chroniclesofww2.features.MatchingGameMessageType
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
    val initiator: MatchingUser,
    val connected: MatchingUser
) {

    var stopListener: StopListener? = null
    var startSessionListener: StartSessionListener? = null

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
            GameFeaturesMessages.ACCEPTED -> {
                if (initiator.login == login) initiatorAgreed = true
                if (connected.login == login) connectedAgreed = true
                if (initiatorAgreed && connectedAgreed) {
                    initiator.client.send(Json.encodeToString(gameData))
                    connected.client.send(Json.encodeToString(gameData.getVersionForAnotherPlayer()))
                    startSessionListener?.onSessionStart(this)
                }
            }
            GameFeaturesMessages.REJECTED, GameFeaturesMessages.CANCEL_CONNECTION -> {
                val string = Json.encodeToString(
                    MatchingGameMessageDTO(
                        type = MatchingGameMessageType.REJECT,
                        message = "Rejected"
                    )
                )
                initiator.client.send(string)
                connected.client.send(string)
                stopListener?.onStop(this)
            }
        }
    }

    init {
        startTimeoutTimer()
    }

    fun startTimeoutTimer() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(TIMEOUT)
            val message = Json.encodeToString(MatchingGameMessageDTO(
                type = MatchingGameMessageType.TIMEOUT,
                message = "Timeout"
            ))
            initiator.client.send(message)
            connected.client.send(message)
            stopListener?.onStop(this@MatchingGame)
        }
    }

    fun interface StartSessionListener {
        suspend fun onSessionStart(matchingGame: MatchingGame)
    }

    fun interface StopListener {
        suspend fun onStop(matchingGame: MatchingGame)
    }

    fun interface MessageHandler {
        suspend fun onMessage(login: String, message: String)
    }

    companion object {
        const val TIMEOUT = 15000L
        const val TIMEOUT_MESSAGE = "Timeout"
    }

}