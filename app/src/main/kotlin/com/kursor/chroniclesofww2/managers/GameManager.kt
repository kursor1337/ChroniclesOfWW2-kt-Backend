package com.kursor.chroniclesofww2.managers

import com.kursor.chroniclesofww2.features.*
import com.kursor.chroniclesofww2.game.GameDataWaiting
import com.kursor.chroniclesofww2.game.GameSession
import com.kursor.chroniclesofww2.model.serializable.GameData
import kotlin.random.Random

const val GAME_ID_UNTIL = 999999
const val GAME_ID_FROM = 100000

class GameManager {

    suspend fun createGame(createGameReceiveDTO: CreateGameReceiveDTO): CreateGameResponseDTO {
        val id = generateGameId()
        val gameDataWaiting = GameDataWaiting(id, createGameReceiveDTO)
        GameController.gameCreated(gameDataWaiting)
        return CreateGameResponseDTO(gameId = id)
    }

    suspend fun initGameSession(joinGameReceiveDTO: JoinGameReceiveDTO): JoinGameResponseDTO {
        val id = joinGameReceiveDTO.gameId
        val gameDataWaiting = getWaitingGameById(id)
            ?: return JoinGameResponseDTO(
                message = GameFeaturesMessages.NO_GAME_WITH_SUCH_ID,
                gameData = null
            )
        if (gameDataWaiting.password != joinGameReceiveDTO.password) return JoinGameResponseDTO(
            message = GameFeaturesMessages.INVALID_PASSWORD,
            gameData = null
        )
        val gameSession = createGameSession(gameDataWaiting, joinGameReceiveDTO).apply {
            listener = object : GameSession.Listener {
                override suspend fun onGameSessionStopped(gameSession: GameSession) {
                    stopGameSession(gameSession)
                }
            }
        }
        GameController.gameInitialized(gameSession)
        return JoinGameResponseDTO(
            message = GameFeaturesMessages.SUCCESS,
            gameData = gameSession.initiatorGameData.getVersionForAnotherPlayer()
        )
    }

    suspend fun stopGameSession(gameSession: GameSession) {
        GameController.gameStopped(gameSession)
    }

    fun getGameSessionById(id: Int): GameSession? = GameController.getCurrentGameSessions()[id]

    fun getWaitingGameById(id: Int): GameDataWaiting? = GameController.getWaitingGames()[id]

    fun startObservingGames(observer: GameControllerObserver) {
        GameController.observers.add(observer)
    }

    fun stopObservingGames(observer: GameControllerObserver) {
        GameController.observers.remove(observer)
    }

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

    interface GameControllerObserver {
        suspend fun onGameSessionInitialized(gameSession: GameSession) {}
        suspend fun onWaitingGameCreated(gameDataWaiting: GameDataWaiting) {}
        suspend fun onGameSessionStopped(gameSession: GameSession) {}
    }

    private object GameController {

        private val currentGameSessions = mutableMapOf<Int, GameSession>()
        private val waitingGames = mutableMapOf<Int, GameDataWaiting>()
        val observers = mutableListOf<GameControllerObserver>()

        suspend fun gameCreated(gameDataWaiting: GameDataWaiting) {
            waitingGames[gameDataWaiting.id] = gameDataWaiting
            observers.forEach { it.onWaitingGameCreated(gameDataWaiting) }
        }

        suspend fun gameInitialized(gameSession: GameSession) {
            currentGameSessions[gameSession.id] = gameSession
            waitingGames.remove(gameSession.id)
            observers.forEach { it.onGameSessionInitialized(gameSession) }
        }

        suspend fun gameStopped(gameSession: GameSession) {
            currentGameSessions.remove(gameSession.id)
            observers.forEach { it.onGameSessionStopped(gameSession) }
        }

        fun getCurrentGameSessions(): Map<Int, GameSession> = currentGameSessions

        fun getWaitingGames(): Map<Int, GameDataWaiting> = waitingGames



    }

}