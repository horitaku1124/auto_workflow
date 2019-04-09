package com.github.horitaku1124.auto_workflow.DataBind

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

class TaskModel {
  var sendKeys: String? = null
  var delay: Long = 0
  companion object {
    fun load(jsonNode: JsonNode?): Optional<TaskModel> {
      if (jsonNode == null) {
        return Optional.empty()
      }
      val obj = TaskModel()

      jsonNode["sendKeys"].also {
        if (it != null) {
          obj.sendKeys = it.textValue()
        }
      }
      jsonNode["delay"].also {
        if (it != null) {
          obj.delay = it.asLong()
        }
      }
      return Optional.of(obj)
    }
  }
}