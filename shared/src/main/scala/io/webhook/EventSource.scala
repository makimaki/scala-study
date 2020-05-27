package io.webhook

sealed trait EventSource {
  val `type`: String
}

case class User(userId: String) extends EventSource {
  override final val `type` = "user"
}

case class IgnorableTypeEventSource(`type`: String) extends EventSource
