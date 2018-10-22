package com.github.horitaku1124.auto_workflow

import java.io.InputStreamReader
import java.io.BufferedReader

class CommandThread(var commandPath: String) : Thread() {
  override fun run() {
    println("[start]CommandThread = ${commandPath}")
    val child = Runtime.getRuntime().exec(commandPath)
//    child.waitFor()

    val reader = BufferedReader(InputStreamReader(child.getInputStream()))
    while(child.isAlive) {
      var line = reader.readLine()
      if (line == null) {
        Thread.sleep(1000)
        println(child.isAlive)
      } else {
        println(line)
      }
    }
    println("[stop]CommandThread = ${commandPath}")
  }
}