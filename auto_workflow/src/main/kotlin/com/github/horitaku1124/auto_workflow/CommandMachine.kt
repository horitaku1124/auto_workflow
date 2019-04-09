package com.github.horitaku1124.auto_workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.horitaku1124.auto_workflow.DataBind.IfStarted
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit


class CommandMachine {
  companion object {
    private val OS = System.getProperty("os.name").toLowerCase()
    private val isWindows = OS.indexOf("win") >= 0
    @JvmStatic
    fun main(args: Array<String>) {

      val readAllLines = Files.readAllLines(Paths.get(args[0])).joinToString("\n")

      val mapper = ObjectMapper()
      val readTree = mapper.readTree(readAllLines)
      val start = readTree.get("start")
      var command = start.textValue()
      println(start)

      var ifStarted = IfStarted.load(readTree)

      var execCommands = if (isWindows) arrayListOf("cmd.exe", "/C") else arrayListOf("/bin/bash", "-c")
//      execCommands.addAll(command.split(" "))
      execCommands.add(command)
//      execCommands.add("\"" + command + "\"")
      var process = ProcessBuilder(execCommands)
          .start()

      var stdin = BufferedReader(InputStreamReader(process.inputStream))
      var stderr = BufferedReader(InputStreamReader(process.errorStream))
      var stdout = process.outputStream.buffered()

      var executedIfStarted = false
      while(process.isAlive) {
        var line: String?
        if (stdin.ready()) {
          line = stdin.readLine()
          println(line)
        } else if (stderr.ready()) {
          line = stderr.readLine()
          System.err.println(line)
        } else {
          if (!executedIfStarted && ifStarted.isPresent) {
            var task = ifStarted.get()
            println(" ifStarted.isPresent")
            executedIfStarted = true
            if (task.delay > 0) {
              TimeUnit.MILLISECONDS.sleep(task.delay)
            }
            stdout.write((task.sendKeys + "\n").toByteArray())
            stdout.flush()
          }

          TimeUnit.MILLISECONDS.sleep(100)
        }
      }

      if (stderr.ready()) {
        System.err.println(stderr.readLine())
      }

      println("Process finished.")
    }
  }
}