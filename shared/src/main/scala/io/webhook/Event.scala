package io.webhook

sealed trait Event {
  val `type`: String
  val timestamp: Long
  val source: EventSource
}

case class MessageEvent(
  timestamp: Long,
  source: EventSource,
  replyToken: String,
  message: Message
) extends Event {
  override final val `type` = "message"
}

case class PostbackEvent(
  timestamp: Long,
  source: EventSource,
  replyToken: String,
  postback: Postback
) extends Event {
  override final val `type` = "postback"
}

/**
 * スグレス で取り扱わない LINE@ イベント
 *
 * @param `type` LINE 側のイベント種類
 */
case class IgnorableTypeEvent(`type`: String) extends Event {
  override val timestamp = 0L
  override val source = IgnorableTypeEventSource("")
}
