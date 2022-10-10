package com.kursor.chroniclesofww2.managers

import com.kursor.chroniclesofww2.features.CreateGameReceiveDTO
import com.kursor.chroniclesofww2.features.CreateGameResponseDTO
import com.kursor.chroniclesofww2.features.WaitingGameInfoDTO
import com.kursor.chroniclesofww2.game.*
import com.kursor.chroniclesofww2.logging.Log
import com.kursor.chroniclesofww2.model.serializable.GameData
import com.kursor.chroniclesofww2.repositories.UserScoreRepository
import io.ktor.server.websocket.*
import org.koin.java.KoinJavaComponent.inject
import kotlin.random.Random

const val GAME_ID_UNTIL = 999999
const val GAME_ID_FROM = 100000

class GameManager() {

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
            startSessionListener = WaitingGame.StartSessionListener {
                val gameSession = createGameSession(it).apply {
                    listener = object : GameSession.Listener {
                        override suspend fun onGameSessionStopped(gameSession: GameSession) {
                            stopGameSession(gameSession)
                        }
                    }
                    startTimeoutTimer()
                }
                GameController.gameInitialized(gameSession)
            }
            startTimeoutTimer()
        }
        GameController.gameCreated(waitingGame)
        return CreateGameResponseDTO(gameId = id)
    }

    suspend fun stopGameSession(gameSession: GameSession) {
        GameController.gameStopped(gameSession)
    }

    suspend fun matchingGame(client: Client) {

    }

    fun getCurrentWaitingGamesInfo(): List<WaitingGameInfoDTO> {
        Log.d("GameManager", "getCurrentWaitingGamesInfo: ${GameController.getWaitingGames()}")
        return GameController.getWaitingGames().map { (id, waitingGame) ->
            WaitingGameInfoDTO(
                id = waitingGame.id,
                initiatorLogin = waitingGame.initiator.login,
                battleData = waitingGame.battle.data
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
        waitingGame: WaitingGame
    ): GameSession {
        val gameData = GameData(
            myName = waitingGame.initiator.login,
            enemyName = waitingGame.connected!!.login,
            battle = waitingGame.battle,
            boardHeight = waitingGame.boardHeight,
            boardWidth = waitingGame.boardWidth,
            invertNations = waitingGame.invertNations,
            meInitiator = true
        )
        return GameSession(
            id = waitingGame.id,
            initiatorGameData = gameData
        )
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

    private object MatchController {

        val userScoreRepository by inject<UserScoreRepository>(UserScoreRepository::class.java)

        val matchingUsers = mutableMapOf<Int, MutableMap<String, MatchingUser>>()

        val matchingGames = mutableSetOf<MatchingGame>()

        suspend fun newMatchingUser(matchingUser: MatchingUser) {
            var thisScoreMatchingUsers = matchingUsers[matchingUser.score] ?: mutableMapOf()
            matchingUsers[matchingUser.score] = thisScoreMatchingUsers
            if (thisScoreMatchingUsers.isNotEmpty()) {
                createMatchingGame(thisScoreMatchingUsers.values.elementAt(0), matchingUser)
                return
            }
            for (i in 0..MATCHING_SCORE_MAX_DIFF) {
                thisScoreMatchingUsers = matchingUsers[matchingUser.score + i] ?: mutableMapOf()
                matchingUsers[matchingUser.score + i] = thisScoreMatchingUsers
                if (thisScoreMatchingUsers.isNotEmpty()) {
                    createMatchingGame(thisScoreMatchingUsers.values.elementAt(0), matchingUser)
                    return
                }
                thisScoreMatchingUsers = matchingUsers[matchingUser.score - i] ?: mutableMapOf()
                matchingUsers[matchingUser.score + i] = thisScoreMatchingUsers
                if (thisScoreMatchingUsers.isNotEmpty()) {
                    createMatchingGame(thisScoreMatchingUsers.values.elementAt(0), matchingUser)
                    return
                }
            }
        }

        fun createMatchingGame(matchingUser1: MatchingUser, matchingUser2: MatchingUser) {
            matchingUsers[matchingUser1.score]?.remove(matchingUser1.login)
            matchingUsers[matchingUser2.score]?.remove(matchingUser2.login)
            matchingGames.add(
                MatchingGame(matchingUser1, matchingUser2).apply {
                    stopListener = MatchingGame.StopListener {
                        matchingGames.remove(it)
                    }
                    startSessionListener = MatchingGame.StartSessionListener {
                        val gameSession = GameSession(
                            id = generateGameId(),
                            initiatorGameData = it.gameData,
                            isMatch = true
                        ).apply {
                            listener = object : GameSession.Listener {
                                override suspend fun onGameSessionStopped(gameSession: GameSession) {
                                    GameController.gameStopped(gameSession)
                                }

                                override suspend fun onMatchOver(winner: String, loser: String) {
                                    userScoreRepository.incrementUserScore(winner)
                                    userScoreRepository.decrementUserScore(loser)
                                }
                            }
                            startTimeoutTimer()
                        }
                        GameController.gameInitialized(gameSession)
                    }
                    startTimeoutTimer()
                }
            )
        }
    }

    companion object {
        fun generateGameId(): Int {
            val random = Random(System.currentTimeMillis())
            var id = random.nextInt(GAME_ID_FROM, GAME_ID_UNTIL)
            while (GameController.getCurrentGameSessions()[id] != null || GameController.getWaitingGames()[id] != null) {
                id = random.nextInt(GAME_ID_FROM, GAME_ID_UNTIL)
            }
            return id
        }
    }
}

const val MATCHING_SCORE_MAX_DIFF = 5