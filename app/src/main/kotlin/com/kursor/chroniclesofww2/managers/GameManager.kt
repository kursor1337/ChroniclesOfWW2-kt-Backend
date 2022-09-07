package com.kursor.chroniclesofww2.managers

import com.kursor.chroniclesofww2.features.*
import com.kursor.chroniclesofww2.game.GameSession
import com.kursor.chroniclesofww2.game.WaitingGame
import com.kursor.chroniclesofww2.logging.Log
import com.kursor.chroniclesofww2.model.serializable.GameData
import io.ktor.server.websocket.*
import kotlin.random.Random

const val GAME_ID_UNTIL = 999999
const val GAME_ID_FROM = 100000

class GameManager {

    suspend fun createGame(
        webSocketServerSession: DefaultWebSocketServerSession,
        createGameReceiveDTO: CreateGameReceiveDTO
    ): CreateGameResponseDTO {
        Log.d("GameManager", "createGame: ")
        val id = generateGameId()
        val waitingGame = WaitingGame(id, webSocketServerSession, createGameReceiveDTO).apply {
            timeoutListener = WaitingGame.TimeoutListener {
                GameController.waitingGameTimedOut(it)
            }
            startTimeoutTimer()
        }
        GameController.gameCreated(waitingGame)
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
            startTimeoutTimer()
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

    fun getCurrentWaitingGamesInfo(): List<WaitingGameInfoDTO> {
        return GameController.getWaitingGames().map { (id, waitingGame) ->
            WaitingGameInfoDTO(
                id = waitingGame.id,
                initiatorLogin = waitingGame.initiator.login
            )
        }
    }

    fun getGameSessionById(id: Int): GameSession? = GameController.getCurrentGameSessions()[id]

    fun getWaitingGameById(id: Int): WaitingGame? = GameController.getWaitingGames()[id]

    fun startObservingGames(observer: GameControllerObserver) {
        GameController.observers.add(observer)
    }

    fun stopObservingGames(observer: GameControllerObserver) {
        GameController.observersToRemove.add(observer)
    }

    private fun createGameSession(
        waitingGame: WaitingGame,
        joinGameReceiveDTO: JoinGameReceiveDTO
    ): GameSession {
        val gameData = GameData(
            waitingGame.initiator.login,
            joinGameReceiveDTO.connectedUserLogin,
            waitingGame.battle,
            waitingGame.boardHeight,
            waitingGame.boardWidth,
        )
        return GameSession(
            id = waitingGame.id,
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
        suspend fun onWaitingGameCreated(waitingGame: WaitingGame) {}
        suspend fun onGameSessionStopped(gameSession: GameSession) {}
        suspend fun onWaitingGameTimedOut(waitingGame: WaitingGame) {}
    }

    private object GameController {

        private val currentGameSessions = mutableMapOf<Int, GameSession>()
        private val waitingGames = mutableMapOf<Int, WaitingGame>()
        val observers = mutableListOf<GameControllerObserver>()
        val observersToRemove = mutableListOf<GameControllerObserver>()

        suspend fun gameCreated(waitingGame: WaitingGame) {
            waitingGames[waitingGame.id] = waitingGame
            observers.forEach { it.onWaitingGameCreated(waitingGame) }
            observers.removeAll(observersToRemove)
        }

        suspend fun gameInitialized(gameSession: GameSession) {
            currentGameSessions[gameSession.id] = gameSession
            waitingGames.remove(gameSession.id)
            observers.forEach { it.onGameSessionInitialized(gameSession) }
            observers.removeAll(observersToRemove)
        }

        suspend fun gameStopped(gameSession: GameSession) {
            currentGameSessions.remove(gameSession.id)
            observers.forEach { it.onGameSessionStopped(gameSession) }
            observers.removeAll(observersToRemove)
        }

        suspend fun waitingGameTimedOut(waitingGame: WaitingGame) {
            waitingGames.remove(waitingGame.id)
            observers.forEach { it.onWaitingGameTimedOut(waitingGame) }
            observers.removeAll(observersToRemove)
        }

        fun getCurrentGameSessions(): Map<Int, GameSession> = currentGameSessions

        fun getWaitingGames(): Map<Int, WaitingGame> = waitingGames


    }

}