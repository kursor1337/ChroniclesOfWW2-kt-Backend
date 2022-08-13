package com.kursor.chroniclesofww2.game

import com.kursor.chroniclesofww2.game.entities.GameDataWaiting
import com.kursor.chroniclesofww2.model.serializable.Battle
import com.kursor.chroniclesofww2.model.serializable.GameData
import kotlinx.serialization.Serializable

@Serializable
data class CreateGameReceiveDTO(
    val initiatorLogin: String,
    val battle: Battle,
    val boardHeight: Int,
    val boardWidth: Int
)

@Serializable
data class CreateGameResponseDTO(
    val gameId: Int
)

@Serializable
data class JoinGameReceiveDTO(
    val connectedUserLogin: String,
    val gameId: Int
)

@Serializable
data class JoinGameResponseDTO(
    val message: String,
    val gameData: GameData?
)


object GameFeaturesMessages {
    const val SUCCESS = "Success"
    const val NO_GAME_WITH_SUCH_ID = "No game with such id"
}