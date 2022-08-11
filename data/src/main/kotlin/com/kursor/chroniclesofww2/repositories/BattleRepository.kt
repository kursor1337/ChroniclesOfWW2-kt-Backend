package com.kursor.chroniclesofww2.repositories

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.db.tables.BattleTable
import com.kursor.chroniclesofww2.entities.Battle
import com.kursor.chroniclesofww2.entities.User
import org.jetbrains.exposed.sql.*

class BattleRepository(
    private val battleTable: BattleTable
) {

    suspend fun getBattleById(id: Int): Battle? = DB.query {
        battleTable.select { battleTable.id eq id }.map { it.toBattle() }.singleOrNull()
    }

    suspend fun getBattlesForUser(user: User): List<Battle> = getBattlesForUser(user.login)

    suspend fun getBattlesForUser(userLogin: String): List<Battle> = DB.query {
        battleTable.select { battleTable.loginOfCreator eq userLogin }.map { it.toBattle() }
    }

    suspend fun getAllBattles(): List<Battle> = DB.query {
        battleTable.selectAll().map { it.toBattle() }
    }

    suspend fun saveBattle(battle: Battle) = DB.query {
        battleTable.insert { row ->
            row[battleTable.id] = battle.id
            row[battleTable.loginOfCreator] = battle.loginOfCreator
            row[battleTable.name] = battle.name
            row[battleTable.description] = battle.description
            row[battleTable.dataJson] = battle.dataJson
        }
    }

    suspend fun updateBattle(battle: Battle) = DB.query {
        battleTable.update({battleTable.id eq battle.id}) {
            it[battleTable.name] = battle.name
            it[battleTable.description] = battle.name
            it[battleTable.dataJson] = battle.dataJson
        }
    }

    suspend fun deleteBattle(id: Int) = DB.query {
        battleTable.deleteWhere { battleTable.id eq id }
    }

    suspend fun deleteBattle(battle: Battle) {
        deleteBattle(battle.id)
    }

    private fun ResultRow.toBattle(): Battle = Battle(
        this[battleTable.id],
        this[battleTable.loginOfCreator],
        this[battleTable.name],
        this[battleTable.description],
        this[battleTable.dataJson]
    )

}
