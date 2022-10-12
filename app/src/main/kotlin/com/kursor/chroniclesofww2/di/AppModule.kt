package com.kursor.chroniclesofww2.di

import com.kursor.chroniclesofww2.managers.BattleManager
import com.kursor.chroniclesofww2.managers.GameManager
import com.kursor.chroniclesofww2.managers.UserManager
import org.koin.dsl.module

val appModule = module {
    single {
        UserManager(
            userRepository = get(),
            userScoreRepository = get()
        )
    }
    single {
        BattleManager(battleRepository = get())
    }
    single {
        GameManager(userScoreRepository = get())
    }

}