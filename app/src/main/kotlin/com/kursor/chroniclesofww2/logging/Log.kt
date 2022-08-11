package com.kursor.chroniclesofww2.logging

import com.kursor.chroniclesofww2.App
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

object Log {
    private const val TAG = "Log"

    private val logFileName: String
    private val file: FileOutputStream
    private val writer: OutputStreamWriter

    init {
        val logFolder = File("logs")
        if (!logFolder.exists()) {
            logFolder.mkdir()
        }
        logFileName = "log-${System.currentTimeMillis()}.txt"
        file = FileOutputStream("logs/$logFileName")
        writer = file.writer()
    }

    private fun writeln(line: String) {
        println(line)
        writer.write(line + "\n")
    }

    fun d(tag: String, message: String) {
        if (!App.instance.appConfig.debug) return
        message.split("\n").forEach {
            writeln("${getTimeDate()} DEBUG $tag: $it")
        }
    }

    fun e(tag: String, message: String) {
        message.split("\n").forEach {
            writeln("${getTimeDate()} ERROR $tag: $it")
        }
    }

    fun f(tag: String, message: String) {
        message.split("\n").forEach {
            writeln("${getTimeDate()} FATAL $tag: $it")
        }
        writeln("${getTimeDate()} FATAL $TAG: FATAL: Can't continue. Exiting with error code 1")
        exitProcess(1)
    }

    fun i(tag: String, message: String) {
        message.split("\n").forEach {
            writeln("${getTimeDate()} INFO $tag: $it")
        }
    }

    private fun getTimeDate(): String = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date())

    fun onDestroy() {
        writer.close()
    }
}