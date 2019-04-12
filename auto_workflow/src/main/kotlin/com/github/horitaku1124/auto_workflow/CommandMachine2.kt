package com.github.horitaku1124.auto_workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.horitaku1124.auto_workflow.DataBind.TaskModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit


class CommandMachine2 {
  companion object {
    private val OS = System.getProperty("os.name").toLowerCase()
    private val isWindows = OS.indexOf("win") >= 0
    @JvmStatic
    fun main(args: Array<String>) {

      val readAllLines = Files.readAllLines(Paths.get(args[0])).joinToString("\n")

      val mapper = ObjectMapper()
      val readTree = mapper.readTree(readAllLines)

      var command = readTree.get("start").textValue()
      var todo = arrayListOf<TaskModel>()


      var ifStarted = TaskModel.load(readTree.findValue("if_started"))
      if (ifStarted.isPresent) {
        todo.add(ifStarted.get())
      }

      var taskValues = readTree.findValues("tasks")
      if (taskValues != null) {
        for (taskValue in taskValues) {
          for (task in taskValue.toMutableList()) {
            var taskModel = TaskModel.load(task)
            if (taskModel.isPresent) {
              todo.add(taskModel.get())
            }
          }
        }
      }
      var finallyTask = TaskModel.load(readTree.findValue("finally"))
      if (finallyTask.isPresent) {
        todo.add(finallyTask.get())
      }


      var execCommands = if (isWindows) arrayListOf("cmd.exe", "/C") else arrayListOf("/bin/bash", "-c")
      execCommands.add(command)
      var process = ProcessBuilder(execCommands)
          .start()


      var stdin = InputStreamReader(process.inputStream)
      var stderr = InputStreamReader(process.errorStream)
      var stdout = process.outputStream.buffered()


      var futureTask: TaskModel? = null
      var executeAt = 0L
      while(process.isAlive) {
        var line: String?
        if (stdin.ready()) {
          var buffer = CharArray(1024)
          var len = stdin.read(buffer)
          print(String(buffer, 0, len))
        } else if (stderr.ready()) {
          var buffer = CharArray(1024)
          var len = stderr.read(buffer)
          System.err.print(String(buffer, 0, len))
        } else {
          if (futureTask == null) {
            if (!todo.isEmpty()) {
              var task = todo[0]
              todo.remove(task)
              if (task.delay > 0) {
                futureTask = task
                executeAt = System.currentTimeMillis() + task.delay
              } else {
                println("${task.sendKeys}")
                stdout.write((task.sendKeys + "\n").toByteArray())
                stdout.flush()
              }
            }
          } else if (executeAt <= System.currentTimeMillis()) {
            println("${futureTask.sendKeys}")
            stdout.write((futureTask.sendKeys + "\n").toByteArray())
            stdout.flush()
            futureTask = null
          }
        }
        TimeUnit.MILLISECONDS.sleep(50)
      }

      if (stderr.ready()) {
        var buffer = CharArray(1024)
        var len = stderr.read(buffer)
        System.err.println(String(buffer, 0, len))
      }

      println("Process finished.")
    }
  }
}