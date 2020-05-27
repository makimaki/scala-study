package io.reply

sealed trait Action {
  val label: String
}

case class PostbackAction(label: String, data: String, displayText: Option[String] = None) extends Action

case class UriAction(uri: String, label: String) extends Action
