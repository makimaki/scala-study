package io.reply

sealed trait Message

case class TextMessage(text: String) extends Message

case class TemplateMessage(altText: String, template: Template) extends Message
