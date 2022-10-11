package com.kursor.chroniclesofww2.di

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.db.tables.BattleTable
import com.kursor.chroniclesofww2.db.tables.UserScoreTable
import com.kursor.chroniclesofww2.db.tables.UserTable
import com.kursor.chroniclesofww2.repositories.BattleRepository
import com.kursor.chroniclesofww2.repositories.UserRepository
import com.kursor.chroniclesofww2.repositories.UserScoreRepository
import org.koin.dsl.module

val dataModule = module {
    single {
        UserTable()
    }
    single {
        UserRepository(userTable = get())
    }
    single {
        BattleTable()
    }
    single {
        BattleRepository(battleTable = get())
    }
    single {
        UserScoreTable()
    }
    single {
        UserScoreRepository(userScoreTable = get())
    }

}