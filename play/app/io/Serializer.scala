package io

import io.reply._
import play.api.libs.json.{Json, Writes}

object Serializer {
  implicit val w: Writes[Request] = (o: Request) =>
    Json.obj(
      "replyToken" -> o.replyToken,
      "messages" -> o.messages.map(Json.toJson(_)(MessageSerializer.w))
    )

  object MessageSerializer {

    val w: Writes[Message] = {
      case m: TextMessage     => TextMessageSerializer.w.writes(m)
      case m: TemplateMessage => TemplateMessageSerializer.w.writes(m)
    }
  }

  object TextMessageSerializer {

    val w: Writes[TextMessage] = (o: TextMessage) =>
      Json.obj(
        "type" -> "text",
        "text" -> o.text
      )
  }

  object TemplateMessageSerializer {

    val w: Writes[TemplateMessage] = (o: TemplateMessage) =>
      Json.obj(
        "type" -> "template",
        "altText" -> o.altText,
        "template" -> Json.toJson(o.template)(TemplateSerializer.w)
      )

    object TemplateSerializer {
      implicit private val actionWrites: Writes[Action] = ActionSerializer.w

      val w: Writes[Template] = {
        case t: ButtonsTemplate =>
          Json.obj(
            "type" -> "buttons",
            "text" -> t.text,
            "actions" -> Json.toJson(t.actions)
          )
      }
    }
  }
  object ActionSerializer {

    val w: Writes[Action] = {
      case a: PostbackAction => PostbackActionSerializer.w.writes(a)
      case a: UriAction      => UriActionSerializer.w.writes(a)
    }
  }

  object PostbackActionSerializer {

    val w: Writes[PostbackAction] = (o: PostbackAction) =>
      Json.obj(
        "type" -> "postback",
        "label" -> o.label,
        "displayText" -> o.displayText.getOrElse(o.label).toString,
        "data" -> o.data
      )
  }

  object UriActionSerializer {

    val w: Writes[UriAction] = (o: UriAction) =>
      Json.obj(
        "type" -> "uri",
        "label" -> o.label,
        "uri" -> o.uri
      )
  }
}
