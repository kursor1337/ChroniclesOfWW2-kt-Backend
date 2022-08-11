package com.kursor.chroniclesofww2.db

import com.kursor.chroniclesofww2.Variables
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction


const val DB_NAME = "Chronicles Of WW2"
const val DB_PORT = 5432

class DB {

    init {
        Database.connect(hikari())
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = "jdbc:postgresql://localhost:$DB_PORT/$DB_NAME"
        config.username = "postgres"
        config.password = Variables.POSTGRES_DB_PASSWORD
        config.maximumPoolSize = 10
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        return HikariDataSource(config)
    }

    companion object {
        suspend fun <T> query(block: () -> T): T =
            withContext(Dispatchers.IO) {
                transaction {
                    addLogger(StdOutSqlLogger)
                    block()
                }
            }
    }
}