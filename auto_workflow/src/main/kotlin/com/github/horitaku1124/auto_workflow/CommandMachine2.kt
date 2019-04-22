package com.github.horitaku1124.auto_workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.horitaku1124.auto_workflow.DataBind.TaskModel
import org.graalvm.polyglot.Context
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit


class CommandMachine2 {
  companion object {
    private val OS = System.getProperty("os.name").toLowerCase()
    private val isWindows = OS.indexOf("win") >= 0
    private const val finishCommand = "exit"
    @JvmStatic
    fun main(args: Array<String>) {
      val readAllLines = Files.readAllLines(Paths.get(args[0])).joinToString("\n")

      val mapper = ObjectMapper()
      val readTree = mapper.readTree(readAllLines)

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


      // TODO separate into another
      var initializeScript = readTree.findValue("initialize_script")
      if (initializeScript != null) {
        var initFile = Paths.get(initializeScript.textValue())
        var beforeLint = arrayListOf("(function(){\n", "let data = 10;")
        var allLine = Files.readAllLines(initFile)
        beforeLint.addAll(allLine)
        beforeLint.add("return JSON.stringify(addTask(data));")
        beforeLint.add("})")
        val context = Context.create("js")
        val function = context.eval("js", beforeLint.joinToString("\n"))
        val jsonStr = function.invokeMember("call")
        println("json=$jsonStr")
        if (jsonStr.isString) {
          val json = mapper.readTree(jsonStr.asString())
          if (json.isArray) {
            for (item in json.iterator()) {
              println("add task=${item.textValue()}")
              val task = TaskModel()
              task.delay = 100
              task.sendKeys = item.textValue()
              todo.add(task)
            }
          } else {
            throw RuntimeException("error")
          }
        } else {
          throw RuntimeException("error")
        }
      }

      var finallyTask = TaskModel.load(readTree.findValue("finally"))
      if (finallyTask.isPresent) {
        todo.add(finallyTask.get())
      }

      var startCommand = readTree.get("start")
      val execCommands: ArrayList<String>
      if (startCommand == null) {
        execCommands = if (isWindows) arrayListOf("cmd.exe") else arrayListOf("/bin/bash")
      } else {
        var command = startCommand.textValue()
        execCommands = if (isWindows) arrayListOf("cmd.exe", "/C") else arrayListOf("/bin/bash", "-c")
        execCommands.add(command)
      }

      var process = ProcessBuilder(execCommands)
          .start()


      var stdin = InputStreamReader(process.inputStream)
      var stderr = InputStreamReader(process.errorStream)
      var stdout = process.outputStream.buffered()
      var sendKeysToStdout = { str:String? ->
        stdout.write(str!!.toByteArray())
        stdout.write((if (isWindows) "\r\n" else "\n").toByteArray())
        stdout.flush()
      }

      var futureTask: TaskModel? = null
      var executeAt = 0L
      var ifMatchTask: TaskModel? = null
      var finishAt = 0L
      while(process.isAlive) {
        var line: String? = null
        if (stdin.ready()) {
          line = readString(stdin)
          print(line)
        } else if (stderr.ready()) {
          line = readString(stderr)
          System.err.print(line)
        } else {
          if (ifMatchTask != null) {
            if (finishAt > 0 && finishAt <= System.currentTimeMillis()) {
              println("time over")
              ifMatchTask = null
            }
          } else if (futureTask == null) {
            if (todo.isNotEmpty()) {
              var task = todo[0]
              todo.remove(task)
              if (task.ifMatchFinish != null) {
                ifMatchTask = task
                println("${task.sendKeys}")
                sendKeysToStdout(task.sendKeys)
                if (task.timeLimit > 0) {
                  finishAt = System.currentTimeMillis() + task.timeLimit
                }
              } else if (task.delay > 0) {
                futureTask = task
                executeAt = System.currentTimeMillis() + task.delay
              } else {
                println("${task.sendKeys}")
                sendKeysToStdout(task.sendKeys)
              }
            } else {
              sendKeysToStdout(finishCommand)
            }
          } else if (executeAt <= System.currentTimeMillis()) {
            println("${futureTask.sendKeys}")
            sendKeysToStdout(futureTask.sendKeys)
            futureTask = null
          }
        }
        if (line != null && ifMatchTask != null) {
          if (ifMatchTask.ifMatchFinish!!.containsMatchIn(line)) {
            ifMatchTask = null
          }
        }
        TimeUnit.MILLISECONDS.sleep(50)
      }

      if (stderr.ready()) {
        System.err.println(readString(stderr))
      }

      println("Process finished.")
    }
    fun readString(input: InputStreamReader, readByte:Int = 1024):String {
      val buffer = CharArray(readByte)
      val len = input.read(buffer)
      return  String(buffer, 0, len)
    }
  }

}