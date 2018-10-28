package com.github.horitaku1124.auto_workflow

import java.io.InputStreamReader
import java.io.BufferedReader

class CommandThread(var commandPath: String) : Thread() {
  var child: Process? = null
  override fun run() {
    println("[start]CommandThread = ${commandPath}")
    child = Runtime.getRuntime().exec(commandPath)
//    child.waitFor()

    val stdin = BufferedReader(InputStreamReader(child!!.inputStream))
    val stderr = BufferedReader(InputStreamReader(child!!.errorStream))
    while(child!!.isAlive) {
      if (stdin.ready()) {
        println(stdin.readLine())
      } else if (stderr.ready()) {
        println(stderr.readLine())
      } else {
        Thread.sleep(100)
      }
    }
    println("[stop]CommandThread = ${commandPath}")
  }
  fun isRunning(): Boolean {
    if (child == null) {
      return false
    }
    return child!!.isAlive
  }

  fun halt() {
    child!!.destroy()
  }
}