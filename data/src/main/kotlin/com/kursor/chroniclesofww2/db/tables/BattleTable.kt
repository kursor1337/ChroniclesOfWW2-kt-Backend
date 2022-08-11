package com.kursor.chroniclesofww2.db.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

class BattleTable : Table("battles") {

    val id: Column<Int> = integer("id")
    val loginOfCreator: Column<String> = varchar("login_of_creator", 25)
    val name: Column<String> = varchar("name", 50)
    val description: Column<String> = text("description")
    val dataJson: Column<String> = text("dataJson")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}