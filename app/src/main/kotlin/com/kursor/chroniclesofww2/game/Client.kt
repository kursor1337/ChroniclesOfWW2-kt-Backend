package com.kursor.chroniclesofww2.game

import com.kursor.chroniclesofww2.model.serializable.Player
import io.ktor.server.websocket.*


data class Client(
    val player: Player,
    val webSocketSession: DefaultWebSocketServerSession
) {

}