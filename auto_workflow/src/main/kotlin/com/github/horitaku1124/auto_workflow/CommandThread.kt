package com.github.horitaku1124.auto_workflow

import java.io.InputStreamReader
import java.io.BufferedReader

class CommandThread(var commandPath: String) : Thread() {
  override fun run() {
    println("[start]CommandThread = ${commandPath}")
    val child = Runtime.getRuntime().exec(commandPath)
//    child.waitFor()

    val stdin = BufferedReader(InputStreamReader(child.inputStream))
    val stderr = BufferedReader(InputStreamReader(child.errorStream))
    while(child.isAlive) {
      val line = stdin.readLine()
      val line2 = stderr.readLine()
      if (line != null) {
        println(line)
      } else if (line2 != null) {
        System.err.println(line2)
      } else {
        Thread.sleep(1000)
      }
    }
    println("[stop]CommandThread = ${commandPath}")
  }
}