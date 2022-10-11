package com.kursor.chroniclesofww2

import com.kursor.chroniclesofww2.db.DB
import com.kursor.chroniclesofww2.di.appModule
import com.kursor.chroniclesofww2.di.dataModule
import com.kursor.chroniclesofww2.logging.Log
import com.kursor.chroniclesofww2.plugins.configureAuthentication
import com.kursor.chroniclesofww2.plugins.configureRouting
import com.kursor.chroniclesofww2.plugins.configureSerialization
import com.kursor.chroniclesofww2.plugins.configureSockets
import com.kursor.chroniclesofww2.repositories.UserRepository
import com.kursor.chroniclesofww2.repositories.UserScoreRepository
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

const val PORT = 8080
const val HOST = "0.0.0.0"

class App {

    lateinit var appConfig: AppConfig
        private set
    lateinit var application: Application
        private set
    var serverUp = false
        private set

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun onConfigLoaded(config: AppConfig) {

        appConfig = config
        Log.i(TAG, "Config loaded. Launching embedded server on localhost on port ${config.port}.")

        embeddedServer(Netty, port = config.port, host = HOST) {
            coroutineScope.launch {
                val userRepository by inject<UserRepository>()
                val userScoreRepository by inject<UserScoreRepository>()
                userScoreRepository.syncWithUserRepository(userRepository)
            }
            install(Koin) {
                modules(appModule, dataModule)
            }
            DB.init()
            application = this
            configureSockets()
            configureSerialization()
            configureRouting()
            configureAuthentication()
        }.start(wait = true)
    }

    fun onDestroy() {
        Log.onDestroy()
    }

    companion object {
        val instance by lazy { App() }

        const val TAG = "App"

        @JvmStatic
        fun main(args: Array<String>) {
            Log.i(TAG, "Starting server")
            val appConfig = AppConfig(args)
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    App.instance.onDestroy()
                }
            )
            App.instance.onConfigLoaded(config = appConfig)
        }
    }
}