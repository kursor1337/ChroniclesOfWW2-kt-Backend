package com.kursor.chroniclesofww2


import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

class AppConfig(args: Array<String>) {
    private val parser = ArgParser("chronicles-of-ww2")
    val port by parser.option(ArgType.Int, "port", "p", "Port server should listen to").default(8080)
    val debug by parser.option(ArgType.Boolean, "debug", "d", "Debug mode").default(false)

    init {
        parser.parse(args)
    }
}