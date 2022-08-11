package com.kursor.chroniclesofww2.repositories

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.db.tables.UserTable
import com.kursor.chroniclesofww2.entities.User
import org.jetbrains.exposed.sql.*

class UserRepository(
    private val userTable: UserTable
) {

    suspend fun getUsersWithUsername(username: String): List<User> = DB.query {
        userTable.select { userTable.username eq username }.map { it.toUser() }
    }

    suspend fun getAllUsers(): List<User> = DB.query {
        userTable.selectAll().map { it.toUser() }
    }

    suspend fun getUserByLogin(login: String): User? = DB.query {
        userTable.select { userTable.login eq login }.map { it.toUser() }.singleOrNull()
    }

    suspend fun saveUser(user: User) = DB.query {
        userTable.insert { row ->
            row[userTable.login] = user.login
            row[userTable.username] = user.username
            row[userTable.passwordHash] = user.passwordHash
        }
    }

    suspend fun updateUser(user: User) = DB.query {
        userTable.update({ userTable.login eq user.login }) {
            it[username] = user.username
            it[passwordHash] = user.passwordHash
        }
    }

    suspend fun deleteUser(login: String) = DB.query {
        userTable.deleteWhere { userTable.login eq login }
    }

    suspend fun deleteUser(user: User) {
        deleteUser(user.login)
    }

    private fun ResultRow.toUser(): User = User(
        this[userTable.login],
        this[userTable.username],
        this[userTable.passwordHash]
    )

}

