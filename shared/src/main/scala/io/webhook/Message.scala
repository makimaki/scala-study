package io.webhook

sealed trait Message {
  val id: String
  val `type`: String

  def getContent: String
}

case class TextMessage(
  id: String,
  text: String
) extends Message {
  override final val `type` = "text"

  override def getContent: String = text
}

case class LocationMessage(
  id: String,
  title: Option[String], // ユーザーが地図上から任意の位置を選択して送信した場合、このフィールドはつかない
  address: Option[String], // ユーザーが地図上から選択した位置の住所が特定不可能な場合、このフィールドはつかない
  latitude: Double,
  longitude: Double
) extends Message {
  override final val `type` = "location"

  override def getContent: String = title.getOrElse(address.getOrElse("unknown location"))
}

/**
 * スグレス で取り扱わない LINE@ メッセージ
 *
 * @param id     メッセージ ID
 * @param `type` LINE 側のメッセージ種類
 */
case class IgnorableTypeMessage(id: String, `type`: String) extends Message {
  override def getContent: String = s"unsupported type. (${this.`type`})"
}
