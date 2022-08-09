package com.kursor.chroniclesofww2.db.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

class UsersTable : Table("users") {

    val login: Column<String> = varchar("login", length = 25)
    val username: Column<String> = varchar("username", length = 25)
    val passwordHash: Column<String> = text("password_hash")

    override val primaryKey: PrimaryKey = PrimaryKey(login)

}