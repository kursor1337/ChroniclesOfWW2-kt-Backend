package com.kursor.chroniclesofww2.game.entities

import com.kursor.chroniclesofww2.model.game.Model
import com.kursor.chroniclesofww2.model.game.RuleManager
import com.kursor.chroniclesofww2.model.serializable.GameData


//player 1 always initiator , it is a host of the game
class GameSession(
    val id: Int,
    val initiatorGameData: GameData
) {

    val initiatorPlayer = initiatorGameData.me
    val connectedPlayer = initiatorGameData.enemy

    val ruleManager = RuleManager(Model(initiatorGameData))

}
