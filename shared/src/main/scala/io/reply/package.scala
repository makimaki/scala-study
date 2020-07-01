package io

import play.api.libs.json._

package object reply {

//  implicit val w: Writes[Request] = (o: Request) =>
  implicit val w: Writes[Request] = new Writes[Request] {
    override def writes(o: Request): JsValue =
      Json.obj(
        "replyToken" -> o.replyToken,
        "messages" -> o.messages.map(Json.toJson(_)(MessageSerializer.w))
      )
  }

  object MessageSerializer {

//    val w: Writes[Message] = {
    val w: Writes[Message] = new Writes[Message] {
      override def writes(o: Message): JsValue =
        o match {
          case m: TextMessage     => TextMessageSerializer.w.writes(m)
          case m: TemplateMessage => TemplateMessageSerializer.w.writes(m)
        }
    }
  }

  object TextMessageSerializer {

//    val w: Writes[TextMessage] = (o: TextMessage) =>
    val w: Writes[TextMessage] = new Writes[TextMessage] {
      override def writes(o: TextMessage): JsValue =
        Json.obj(
          "type" -> "text",
          "text" -> o.text
        )
    }
  }

  object TemplateMessageSerializer {

//    val w: Writes[TemplateMessage] = (o: TemplateMessage) =>
    val w: Writes[TemplateMessage] = new Writes[TemplateMessage] {
      override def writes(o: TemplateMessage): JsValue =
        Json.obj(
          "type" -> "template",
          "altText" -> o.altText,
          "template" -> Json.toJson(o.template)(TemplateSerializer.w)
        )
    }

    object TemplateSerializer {
      implicit private val actionWrites: Writes[Action] = ActionSerializer.w

//      val w: Writes[Template] = {
      val w: Writes[Template] = new Writes[Template] {
        override def writes(o: Template): JsValue =
          o match {
            case t: ButtonsTemplate =>
              Json.obj(
                "type" -> "buttons",
                "text" -> t.text,
                "actions" -> Json.toJson(t.actions)
              )
          }
      }
    }
  }
  object ActionSerializer {

//    val w: Writes[Action] = {
    val w: Writes[Action] = new Writes[Action] {
      override def writes(o: Action): JsValue =
        o match {
          case a: PostbackAction => PostbackActionSerializer.w.writes(a)
          case a: UriAction      => UriActionSerializer.w.writes(a)
        }
    }
  }

  object PostbackActionSerializer {

//    val w: Writes[PostbackAction] = (o: PostbackAction) =>
    val w: Writes[PostbackAction] = new Writes[PostbackAction] {
      override def writes(o: PostbackAction): JsValue =
        Json.obj(
          "type" -> "postback",
          "label" -> o.label,
          "data" -> o.data
        ) ++ JsObject(
          o.displayText.toSeq.map(JsString).map("displayText" -> _)
        )
    }
  }

  object UriActionSerializer {

//    val w: Writes[UriAction] = (o: UriAction) =>
    val w: Writes[UriAction] = new Writes[UriAction] {
      override def writes(o: UriAction): JsValue =
        Json.obj(
          "type" -> "uri",
          "label" -> o.label,
          "uri" -> o.uri
        )
    }
  }
}
