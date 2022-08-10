package com.kursor.chroniclesofww2.repositories

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.db.tables.UsersTable
import com.kursor.chroniclesofww2.entities.User
import org.jetbrains.exposed.sql.*

class UserRepository(
    private val db: DB,
    private val usersTable: UsersTable
) {

    suspend fun getUsersWithUsername(username: String): List<User> {
        return db.dbQuery {
            usersTable.select { usersTable.username.eq(username) }.map { it.toUser() }
        }
    }

    suspend fun getAllUsers(): List<User> {
        return db.dbQuery {
            usersTable.selectAll().map { it.toUser() }
        }
    }

    suspend fun getUserByLogin(login: String): User? {
        return db.dbQuery {
            usersTable.select { usersTable.login.eq(login) }.map { it.toUser() }.singleOrNull()
        }
    }

    suspend fun saveUser(user: User) {
        db.dbQuery {
            usersTable.insert { statement ->
                statement[usersTable.login] = user.login
                statement[usersTable.username] = user.username
                statement[usersTable.passwordHash] = user.passwordHash
            }
        }
    }

    suspend fun updateUser(user: User) {
        db.dbQuery {
            usersTable.update({ usersTable.login eq user.login }) {
                it[username] = user.username
                it[passwordHash] = user.passwordHash
            }
        }
    }

    suspend fun deleteUser(login: String) {
        db.dbQuery {
            usersTable.deleteWhere { usersTable.login eq login }
        }
    }

    suspend fun deleteUser(user: User) {
        deleteUser(user.login)
    }

    fun ResultRow.toUser(): User = User(
        this[usersTable.login],
        this[usersTable.username],
        this[usersTable.passwordHash]
    )

}

