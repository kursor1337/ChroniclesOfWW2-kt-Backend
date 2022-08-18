package com.kursor.chroniclesofww2.plugins

import com.kursor.chroniclesofww2.managers.BattleManager
import com.kursor.chroniclesofww2.managers.GameManager
import com.kursor.chroniclesofww2.managers.UserManager
import com.kursor.chroniclesofww2.routes.accountRoutiing
import com.kursor.chroniclesofww2.routes.userRouting
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userManager by inject<UserManager>()
    val battleManager by inject<BattleManager>()
    val gameManager by inject<GameManager>()
    userRouting(userManager)
    accountRoutiing(userManager)
}
