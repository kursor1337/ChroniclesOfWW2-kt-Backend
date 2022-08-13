package com.kursor.chroniclesofww2.repositories

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.db.tables.BattleTable
import com.kursor.chroniclesofww2.entities.BattleDAO
import com.kursor.chroniclesofww2.entities.User
import org.jetbrains.exposed.sql.*

class BattleRepository(
    private val battleTable: BattleTable
) {

    suspend fun getBattleById(id: Int): BattleDAO? = DB.query {
        battleTable.select { battleTable.id eq id }.map { it.toBattle() }.singleOrNull()
    }

    suspend fun getBattlesOfUser(user: User): List<BattleDAO> = getBattlesOfUser(user.login)

    suspend fun getBattlesOfUser(userLogin: String): List<BattleDAO> = DB.query {
        battleTable.select { battleTable.loginOfCreator eq userLogin }.map { it.toBattle() }
    }

    suspend fun getAllBattles(): List<BattleDAO> = DB.query {
        battleTable.selectAll().map { it.toBattle() }
    }

    suspend fun saveBattle(battleDAO: BattleDAO) = DB.query {
        battleTable.insert { row ->
            row[battleTable.id] = battleDAO.id
            row[battleTable.loginOfCreator] = battleDAO.loginOfCreator
            row[battleTable.name] = battleDAO.name
            row[battleTable.description] = battleDAO.description
            row[battleTable.dataJson] = battleDAO.dataJson
        }
    }

    suspend fun updateBattle(battleDAO: BattleDAO) = DB.query {
        battleTable.update({battleTable.id eq battleDAO.id}) {
            it[battleTable.name] = battleDAO.name
            it[battleTable.description] = battleDAO.name
            it[battleTable.dataJson] = battleDAO.dataJson
        }
    }

    suspend fun deleteBattle(id: Int) = DB.query {
        battleTable.deleteWhere { battleTable.id eq id }
    }

    suspend fun deleteBattle(battleDAO: BattleDAO) {
        deleteBattle(battleDAO.id)
    }

    private fun ResultRow.toBattle(): BattleDAO = BattleDAO(
        this[battleTable.id],
        this[battleTable.loginOfCreator],
        this[battleTable.name],
        this[battleTable.description],
        this[battleTable.dataJson]
    )

}
