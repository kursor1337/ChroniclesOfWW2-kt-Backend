package com.kursor.chroniclesofww2.db.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

class UserScoreTable : Table("userscores") {

    val login: Column<String> = varchar("login", length = 25)
    val score: Column<Int> = integer("score")

    override val primaryKey = PrimaryKey(login)

}