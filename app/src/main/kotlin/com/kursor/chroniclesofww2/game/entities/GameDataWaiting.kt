package com.kursor.chroniclesofww2.game.entities

import com.kursor.chroniclesofww2.game.CreateGameReceiveDTO
import com.kursor.chroniclesofww2.model.serializable.Battle

class GameDataWaiting(
    val id: Int,
    val initiatorLogin: String,
    val battle: Battle,
    val boardHeight: Int,
    val boardWidth: Int
) {

    constructor(id: Int, createGameReceiveDTO: CreateGameReceiveDTO): this(
        id,
        createGameReceiveDTO.initiatorLogin,
        createGameReceiveDTO.battle,
        createGameReceiveDTO.boardHeight,
        createGameReceiveDTO.boardWidth
    )

}