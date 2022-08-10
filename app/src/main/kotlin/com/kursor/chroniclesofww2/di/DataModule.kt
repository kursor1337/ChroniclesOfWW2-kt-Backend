package com.kursor.chroniclesofww2.di

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.db.tables.UsersTable
import com.kursor.chroniclesofww2.repositories.UserRepository
import org.koin.dsl.module
import kotlin.math.sin

val dataModule = module {
    single {
        DB()
    }
    single {
        UsersTable()
    }
    single {
        UserRepository(usersTable = get())
    }

}