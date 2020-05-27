package io

case class LineConfig(
  channelAccessToken: String,
  channelSecret: String
) {
  final val SignatureHeaderName = "X-Line-Signature"
  final val ReplyApiEndpoint = "https://api.line.me/v2/bot/message/reply"
}

object LineConfig {
  def fromEnv: LineConfig =
    LineConfig(
      channelAccessToken = sys.env.getOrElse(
        "LINE_CHANNEL_ACCESS_TOKEN",
        throw new IllegalArgumentException("required environment variable: LINE_CHANNEL_ACCESS_TOKEN")
      ),
      channelSecret = sys.env.getOrElse(
        "LINE_CHANNEL_SECRET",
        throw new IllegalArgumentException("required environment variable: LINE_CHANNEL_SECRET")
      )
    )
}
