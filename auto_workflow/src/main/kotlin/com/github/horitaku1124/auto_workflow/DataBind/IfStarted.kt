package com.github.horitaku1124.auto_workflow.DataBind

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

class IfStarted {
  var sendKeys: String? = null
  companion object {
    fun load(jsonNode: JsonNode): Optional<IfStarted> {
      val isStarted = jsonNode.findValue("if_started") ?: return Optional.empty()
      val obj = IfStarted()

      isStarted["sendKeys"].also {
        if (it != null) {
          obj.sendKeys = it.textValue()
        }
      }
      return Optional.of(obj)
    }
  }
}