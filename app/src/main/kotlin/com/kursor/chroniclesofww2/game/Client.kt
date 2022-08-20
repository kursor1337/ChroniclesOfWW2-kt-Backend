package com.kursor.chroniclesofww2.game

import com.kursor.chroniclesofww2.features.GameSessionDTO
import com.kursor.chroniclesofww2.model.serializable.Player
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


data class Client(
    val login: String,
    val webSocketSession: DefaultWebSocketServerSession
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun send(gameSessionDTO: GameSessionDTO) {
        if (webSocketSession.outgoing.isClosedForSend) return
        webSocketSession.send(Json.encodeToString(gameSessionDTO))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun send(string: String) {
        if (webSocketSession.outgoing.isClosedForSend) return
        webSocketSession.send(string)
    }

}