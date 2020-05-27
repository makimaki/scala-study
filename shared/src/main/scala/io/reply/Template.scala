package io.reply

sealed trait Template

case class ButtonsTemplate(
  text: String,
  actions: Seq[Action]
) extends Template
