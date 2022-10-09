package com.kursor.chroniclesofww2.repositories

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.db.tables.BattleTable
import com.kursor.chroniclesofww2.entities.BattleDatabaseEntity
import com.kursor.chroniclesofww2.entities.User
import org.jetbrains.exposed.sql.*

class BattleRepository(
    private val battleTable: BattleTable
) {

    suspend fun getBattleById(id: Int): BattleDatabaseEntity? = DB.query {
        battleTable.select { battleTable.id eq id }.map { it.toBattle() }.singleOrNull()
    }

    suspend fun getBattlesOfUser(user: User): List<BattleDatabaseEntity> = getBattlesOfUser(user.login)

    suspend fun getBattlesOfUser(userLogin: String): List<BattleDatabaseEntity> = DB.query {
        battleTable.select { battleTable.loginOfCreator eq userLogin }.map { it.toBattle() }
    }

    suspend fun getAllBattles(): List<BattleDatabaseEntity> = DB.query {
        battleTable.selectAll().map { it.toBattle() }
    }

    suspend fun saveBattle(battleDatabaseEntity: BattleDatabaseEntity) = DB.query {
        battleTable.insert { row ->
            row[battleTable.id] = battleDatabaseEntity.id
            row[battleTable.loginOfCreator] = battleDatabaseEntity.loginOfCreator
            row[battleTable.name] = battleDatabaseEntity.name
            row[battleTable.description] = battleDatabaseEntity.description
            row[battleTable.dataJson] = battleDatabaseEntity.dataJson
        }
    }

    suspend fun updateBattle(battleDatabaseEntity: BattleDatabaseEntity) = DB.query {
        battleTable.update({battleTable.id eq battleDatabaseEntity.id}) {
            it[battleTable.name] = battleDatabaseEntity.name
            it[battleTable.description] = battleDatabaseEntity.name
            it[battleTable.dataJson] = battleDatabaseEntity.dataJson
        }
    }

    suspend fun deleteBattle(id: Int) = DB.query {
        battleTable.deleteWhere { battleTable.id eq id }
    }

    suspend fun deleteBattle(battleDatabaseEntity: BattleDatabaseEntity) {
        deleteBattle(battleDatabaseEntity.id)
    }

    private fun ResultRow.toBattle(): BattleDatabaseEntity = BattleDatabaseEntity(
        this[battleTable.id],
        this[battleTable.loginOfCreator],
        this[battleTable.name],
        this[battleTable.description],
        this[battleTable.dataJson]
    )

}
