package service

import io.{LineConfig, reply, webhook}

import scala.concurrent.ExecutionContext

class WebhookHandler {
  def handle(request: webhook.Request)(implicit ec: ExecutionContext, lineConfig: LineConfig): Iterator[reply.Request] =
    request.events.iterator.flatMap {
      case webhook.MessageEvent(_, _, replyToken, message) =>
        val replyMessages = message match {
          case webhook.TextMessage(_, "あなたは誰ですか？") =>
            reply.TextMessage("私は scala(play) で実装された何かです。") +: Nil
          case webhook.TextMessage(_, text) =>
            reply.TextMessage(s"$text ですね。わかります。") +: Nil
          case webhook.LocationMessage(_, title, _, latitude, longitude) =>
            val targetTitle = title.getOrElse("(不明)")
            val text = s"$targetTitle ですね。Google Maps で開きたいですか？"
            val googleMapsUrl = s"https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"

            reply.TemplateMessage(
              "これはだいたいてきすと",
              reply.ButtonsTemplate(
                text,
                reply.PostbackAction(
                  label = "是非",
                  displayText = Some("お願いします！"),
                  data = googleMapsUrl
                ) +: Nil
              )
            ) +: Nil
          case _ =>
            reply.TextMessage(s"すみません。よくわかりませんのでダンプします。\n$message") +: Nil
        }

        Some(reply.Request(replyToken, replyMessages))
      case webhook.PostbackEvent(_, _, replyToken, webhook.Postback(data)) =>
        val replyRequest = reply.Request(
          replyToken,
          reply.TemplateMessage(
            "これはだいたいてきすと",
            reply.ButtonsTemplate(
              data,
              reply.UriAction(
                data,
                "ぐぐるまぷ"
              ) +: Nil
            )
          ) +: Nil
        )
        Some(replyRequest)
      case _ =>
        None
    }
}
