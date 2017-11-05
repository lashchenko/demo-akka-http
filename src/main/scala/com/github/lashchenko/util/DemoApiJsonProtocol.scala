package com.github.lashchenko.util

import java.util.UUID

import com.github.lashchenko.model.{ DemoEntity, DemoError }
import spray.json.{ DefaultJsonProtocol, JsString, JsValue, RootJsonFormat, deserializationError }

trait DemoApiJsonProtocol extends DefaultJsonProtocol {

  implicit val DemoUuidFormat = new RootJsonFormat[UUID] {
    def write(obj: UUID): JsValue = JsString(obj.toString)

    def read(jsValue: JsValue): UUID = jsValue match {
      case JsString(id) ⇒ java.util.UUID.fromString(id)
      case _ ⇒ deserializationError("UUID expected")
    }
  }

  implicit val DemoEntityFormat = jsonFormat5(DemoEntity)
  implicit val DemoErrorFormat = jsonFormat1(DemoError)
}

object DemoApiJsonProtocol extends DefaultJsonProtocol
