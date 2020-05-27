package io.reply

case class Request(replyToken: String, messages: Seq[Message])
