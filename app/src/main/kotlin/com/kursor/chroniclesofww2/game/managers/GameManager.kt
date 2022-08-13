package com.kursor.chroniclesofww2.game.managers

import com.kursor.chroniclesofww2.game.*
import com.kursor.chroniclesofww2.game.entities.GameDataWaiting
import com.kursor.chroniclesofww2.game.entities.GameSession
import com.kursor.chroniclesofww2.model.serializable.GameData
import kotlin.random.Random

const val GAME_ID_UNTIL = 999999
const val GAME_ID_FROM = 100000

class GameManager {

    suspend fun createGame(createGameReceiveDTO: CreateGameReceiveDTO): CreateGameResponseDTO {
        val id = generateGameId()
        val gameDataWaiting = GameDataWaiting(id, createGameReceiveDTO)
        GameController.waitingGames[id] = gameDataWaiting
        return CreateGameResponseDTO(gameId = id)
    }

    suspend fun initGameSession(joinGameReceiveDTO: JoinGameReceiveDTO): JoinGameResponseDTO {
        val id = joinGameReceiveDTO.gameId
        val gameDataWaiting = getWaitingGameById(id)
            ?: return JoinGameResponseDTO(
                message = GameFeaturesMessages.NO_GAME_WITH_SUCH_ID,
                gameData = null
            )
        val gameSession = createGameSession(gameDataWaiting, joinGameReceiveDTO)
        GameController.currentGameSessions[id] = gameSession
        GameController.waitingGames.remove(id)
        return JoinGameResponseDTO(
            message = GameFeaturesMessages.SUCCESS,
            gameData = gameSession.initiatorGameData.getVersionForAnotherPlayer()
        )
    }

    fun getGameSessionById(id: Int): GameSession? = GameController.currentGameSessions[id]

    fun getWaitingGameById(id: Int): GameDataWaiting? = GameController.waitingGames[id]

    private fun createGameSession(gameDataWaiting: GameDataWaiting, joinGameReceiveDTO: JoinGameReceiveDTO): GameSession {
        val gameData = GameData(
            gameDataWaiting.initiatorLogin,
            joinGameReceiveDTO.connectedUserLogin,
            gameDataWaiting.battle,
            gameDataWaiting.boardHeight,
            gameDataWaiting.boardWidth,
        )
        return GameSession(
            id = gameDataWaiting.id,
            initiatorGameData = gameData
        )
    }

    private fun generateGameId(): Int {
        val random = Random(System.currentTimeMillis())
        var id = random.nextInt(GAME_ID_FROM, GAME_ID_UNTIL)
        while (getGameSessionById(id) != null || getWaitingGameById(id) != null) {
            id = random.nextInt(GAME_ID_FROM, GAME_ID_UNTIL)
        }
        return id
    }

    private object GameController {

        val currentGameSessions = mutableMapOf<Int, GameSession>()
        val waitingGames = mutableMapOf<Int, GameDataWaiting>()

    }

}