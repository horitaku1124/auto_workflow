package com.github.horitaku1124.auto_workflow

import java.io.InputStreamReader
import java.io.BufferedReader

class CommandThread(var commandPath: String) : Thread() {
  private val OS = System.getProperty("os.name").toLowerCase()
  private val isWindows = OS.indexOf("win") >= 0
  var child: Process? = null
  var processId: Int? = null
  val getPid = """PID=(\d+)""".toRegex()
  override fun run() {
    println("[start]CommandThread = ${commandPath}")
    commandPath = if (isWindows) "signal.exe ${commandPath}" else "./signal ${commandPath}"
    child = Runtime.getRuntime().exec(commandPath)
//    child.waitFor()

    val stdin = BufferedReader(InputStreamReader(child!!.inputStream))
    val stderr = BufferedReader(InputStreamReader(child!!.errorStream))
    var mayPid = stdin.readLine()
    var match = getPid.find(mayPid)
    if (match != null) {
      var pid = match.groups[1]?.value
      processId = pid?.toInt()
    }

    while(child!!.isAlive) {
      if (stdin.ready()) {
        println(stdin.readLine())
      } else if (stderr.ready()) {
        println(stderr.readLine())
      } else {
        Thread.sleep(100)
      }
    }
    while(stdin.ready()) {
      println(stdin.readLine())
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

  fun sendInt() {
    println("[kill]start ${processId}")
    var command = if (isWindows) "Taskkill /PID  ${processId}" else "kill -SIGINT ${processId}"
    var interrupt = Runtime.getRuntime().exec(command)
    val stdin = BufferedReader(InputStreamReader(interrupt.inputStream))
    val stderr = BufferedReader(InputStreamReader(interrupt.errorStream))
    while(interrupt.isAlive) {
      if (stdin.ready()) {
        println(stdin.readLine())
      } else if (stderr.ready()) {
        println(stderr.readLine())
      } else {
        Thread.sleep(100)
      }
    }
    println("[kill]end")
  }
}