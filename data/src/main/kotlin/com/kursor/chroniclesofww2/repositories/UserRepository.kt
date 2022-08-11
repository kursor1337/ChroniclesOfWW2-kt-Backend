package com.kursor.chroniclesofww2.repositories

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.db.tables.UsersTable
import com.kursor.chroniclesofww2.entities.User
import org.jetbrains.exposed.sql.*

class UserRepository(
    private val usersTable: UsersTable
) {

    suspend fun getUsersWithUsername(username: String): List<User> = DB.query {
        usersTable.select { usersTable.username eq username }.map { it.toUser() }
    }

    suspend fun getAllUsers(): List<User> = DB.query {
        usersTable.selectAll().map { it.toUser() }
    }

    suspend fun getUserByLogin(login: String): User? = DB.query {
        usersTable.select { usersTable.login eq login }.map { it.toUser() }.singleOrNull()
    }

    suspend fun saveUser(user: User) {
        DB.query {
            usersTable.insert { row ->
                row[usersTable.login] = user.login
                row[usersTable.username] = user.username
                row[usersTable.passwordHash] = user.passwordHash
            }
        }
    }

    suspend fun updateUser(user: User) {
        DB.query {
            usersTable.update({ usersTable.login eq user.login }) {
                it[username] = user.username
                it[passwordHash] = user.passwordHash
            }
        }
    }

    suspend fun deleteUser(login: String) {
        DB.query {
            usersTable.deleteWhere { usersTable.login eq login }
        }
    }

    suspend fun deleteUser(user: User) {
        deleteUser(user.login)
    }

    private fun ResultRow.toUser(): User = User(
        this[usersTable.login],
        this[usersTable.username],
        this[usersTable.passwordHash]
    )

}

