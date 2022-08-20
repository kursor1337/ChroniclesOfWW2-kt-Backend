package com.kursor.chroniclesofww2.game

import com.kursor.chroniclesofww2.features.CreateGameReceiveDTO
import com.kursor.chroniclesofww2.features.GameFeaturesMessages
import com.kursor.chroniclesofww2.model.serializable.Battle
import io.ktor.server.websocket.*
import kotlinx.coroutines.delay
import java.lang.ref.ReferenceQueue

class WaitingGame(
    val id: Int,
    val initiatorLogin: String,
    val password: String,
    val battle: Battle,
    val boardHeight: Int,
    val boardWidth: Int,
    val initiator: Client
) {

    constructor(id: Int, webSocketSession: DefaultWebSocketServerSession, createGameReceiveDTO: CreateGameReceiveDTO): this(
        id,
        createGameReceiveDTO.password,
        createGameReceiveDTO.initiatorLogin,
        createGameReceiveDTO.battle,
        createGameReceiveDTO.boardHeight,
        createGameReceiveDTO.boardWidth,
        initiator = Client(
            createGameReceiveDTO.initiatorLogin,
            webSocketSession
        )
    )

    var timeoutListener: TimeoutListener? = null

    suspend fun startTimeoutTimer() {
        delay(TIMEOUT)
        timeoutListener?.onTimeout(this)
    }

    suspend fun requestForAccept() {
        initiator.send(GameFeaturesMessages.)
    }



    fun interface TimeoutListener {
        suspend fun onTimeout(waitingGame: WaitingGame)
    }

    fun interface MessageHandler {
        fun onMessage(login: String, message: String)
    }

    companion object {
        const val TIMEOUT = 300000L
    }

}