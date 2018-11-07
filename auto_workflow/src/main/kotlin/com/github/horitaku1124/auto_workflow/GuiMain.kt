package com.github.horitaku1124.auto_workflow

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory

class GuiMain : Application() {
  private var execButton: Button = Button("Exe")
  private var stopButton: Button = Button("Stop")
  private var commandEdit: TextArea = TextArea("")
  private var logPath: TextArea = TextArea("")
  private val listView = ListView<String>()
  private var commands = arrayListOf<String>()
  private var threads = arrayOfNulls<CommandThread>(3)

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Workflow"

    val mainPane = BorderPane()
    val rightPane = BorderPane()
    val commandPane = BorderPane()

    var tasks = getTasks("./src/main/resources/tasks.xml")

    for (task in tasks) {
      listView.items.add(task.first)
      commands.add(task.second!!)
    }


    listView.setOnMouseClicked {
      var index = listView.selectionModel.selectedIndices[0]
      commandEdit.text = commands[index]
    }
    execButton.setOnMouseClicked {
      if(!listView.selectionModel.selectedIndices.isEmpty()) {
        var index = listView.selectionModel.selectedIndices[0]
        if (threads[index] == null) {
          threads[index] = CommandThread(commands[index])
          threads[index]?.start()
        } else if(threads[index]!!.isRunning()) {
          threads[index]!!.halt()
          threads[index] = null;
        }
      }
    }
    stopButton.setOnMouseClicked {
      var index = listView.selectionModel.selectedIndices[0]
      if(threads[index]!!.isRunning()) {
        threads[index]!!.sendInt()
      }
    }
    commandEdit.textProperty().addListener { observable, oldValue, newValue ->
      if(!listView.selectionModel.selectedIndices.isEmpty()) {
        var index = listView.selectionModel.selectedIndices[0]
        commands[index] = commandEdit.text
      }
    }

    commandEdit.isWrapText = true
    commandPane.left = execButton
    commandPane.center = stopButton
    rightPane.top = commandPane
    rightPane.center = commandEdit
    rightPane.bottom = logPath

    val hbox = HBox(listView)
    mainPane.left = hbox
    mainPane.right = rightPane

    val scene = Scene(mainPane, 600.0, 400.0)
    primaryStage.scene = scene
    primaryStage.show()
  }

  fun getTasks(filePath: String): ArrayList<Pair<String?, String?>> {
    val factory = DocumentBuilderFactory.newInstance()

    factory.isIgnoringComments = true
    factory.isIgnoringElementContentWhitespace = true
    factory.isValidating = false

    val builder = factory.newDocumentBuilder()

    var list:ArrayList<Pair<String?, String?>> = arrayListOf()

    var dom = builder.parse(InputSource(filePath))
    var elems = dom.getElementsByTagName("task")

    for (i in 0 until elems.length) {
      var elm = elems.item(i)
      var attr = elm.attributes
      var name = attr.getNamedItem("name").textContent
      var task = Pair(name, elm.textContent.trim())
      list.add(task)
    }

    return list
  }
}

fun main(args: Array<String>) {
  Application.launch(GuiMain::class.java, *args)
}