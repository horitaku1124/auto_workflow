package com.github.horitaku1124.auto_workflow

import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import javafx.beans.value.ObservableValue




class GuiMain : Application() {
  private var execButton: Button = Button("Exe")
  private var commandEdit: TextArea = TextArea("")
  private var logPath: TextArea = TextArea("")
  private val listView = ListView<String>()
  private var commands = mutableListOf("tail -f /var/log/system.log", "ping 192.168.1.1", "c")
  private var threads = arrayOfNulls<CommandThread>(3)

  override fun start(primaryStage: Stage) {
    primaryStage.title = "Workflow"

    val mainPane = BorderPane()
    val rightPane = BorderPane()

    listView.items.add("Item 1")
    listView.items.add("Item 2")
    listView.items.add("Item 3")

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
        }
      }
    }
    commandEdit.textProperty().addListener(object : ChangeListener<String> {
      override fun changed(observable: ObservableValue<out String>, oldValue: String, newValue: String) {

        if(!listView.selectionModel.selectedIndices.isEmpty()) {
          var index = listView.selectionModel.selectedIndices[0]
          commands[index] = commandEdit.text
        }
      }
    })

    commandEdit.isWrapText = true
    rightPane.top = execButton
    rightPane.center = commandEdit
    rightPane.bottom = logPath

    val hbox = HBox(listView)
    mainPane.left = hbox
    mainPane.right = rightPane

    val scene = Scene(mainPane, 600.0, 400.0)
    primaryStage.scene = scene
    primaryStage.show()
  }
}

fun main(args: Array<String>) {
  Application.launch(GuiMain::class.java, *args)
}