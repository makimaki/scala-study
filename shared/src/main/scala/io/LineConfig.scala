package io

case class LineConfig(
  channelAccessToken: String,
  channelSecret: String,
  replyApiEndpoint: String
) {
  final val SignatureHeaderName = "X-Line-Signature"
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
      ),
      replyApiEndpoint = sys.env.getOrElse(
        "LINE_REPLY_API_ENDPOINT",
        throw new IllegalArgumentException("required environment variable: LINE_REPLY_API_ENDPOINT")
      )
    )
}
