package com.kursor.chroniclesofww2.repositories

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.db.tables.UserScoreTable
import com.kursor.chroniclesofww2.entities.UserScore
import org.jetbrains.exposed.sql.*

const val DEFAULT_USER_SCORE = 10

class UserScoreRepository(
    private val userScoreTable: UserScoreTable
) {

    suspend fun getUserLoginsWithScore(score: Int): List<String> = DB.query {
        userScoreTable.select { userScoreTable.score eq score }.map { it.toUserScore().login }
    }

    suspend fun getAllUserScores(): List<UserScore> = DB.query {
        userScoreTable.selectAll().map { it.toUserScore() }
    }

    suspend fun getUserScoreByLogin(login: String): UserScore? = DB.query {
        userScoreTable.select { userScoreTable.login eq login }.map { it.toUserScore() }.singleOrNull()
    }

    suspend fun saveUserScore(userScore: UserScore) = DB.query {
        userScoreTable.insert { row ->
            row[userScoreTable.login] = userScore.login
            row[userScoreTable.score] = userScore.score
        }
    }

    suspend fun updateUserScore(userScore: UserScore) = DB.query {
        userScoreTable.update({ userScoreTable.login eq userScore.login }) {
            it[score] = userScore.score
        }
    }

    suspend fun deleteUserScore(login: String) = DB.query {
        userScoreTable.deleteWhere { userScoreTable.login eq login }
    }

    suspend fun deleteUserScore(userScore: UserScore) {
        deleteUserScore(userScore.login)
    }

    suspend fun incrementUserScore(login: String) {
        val cur = getUserScoreByLogin(login)
        if (cur == null) saveUserScore(UserScore(login, DEFAULT_USER_SCORE + 1))
        else updateUserScore(UserScore(login, cur.score + 1))
    }

    suspend fun decrementUserScore(login: String) {
        val cur = getUserScoreByLogin(login)
        if (cur == null) saveUserScore(UserScore(login, DEFAULT_USER_SCORE - 1))
        else updateUserScore(UserScore(login, cur.score - 1))
    }

    suspend fun syncWithUserRepository(userRepository: UserRepository) {
        val userScoreSet = mutableSetOf<UserScore>()
        userRepository.getAllUsers().forEach {
            var userScore = getUserScoreByLogin(it.login)
            if (userScore == null) {
                userScore = UserScore(it.login, DEFAULT_USER_SCORE)
                saveUserScore(userScore)
            }
            userScoreSet.add(userScore)
        }
        getAllUserScores().forEach {
            if (it !in userScoreSet) deleteUserScore(it)
        }
    }

    private fun ResultRow.toUserScore(): UserScore = UserScore(
        this[userScoreTable.login],
        this[userScoreTable.score]
    )

}