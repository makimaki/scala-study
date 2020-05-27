package controllers

import java.nio.charset.StandardCharsets
import javax.inject.Inject

import infrastructure.LineClient
import io.Deserializer._
import io.LineConfig
import io.webhook.Request
import play.api.libs.json.Json
import play.api.mvc.InjectedController

import scala.concurrent.{ExecutionContext, Future}

class LineController @Inject() (
  validator: io.Validator,
  handler: service.WebhookHandler,
  lineClient: LineClient
)(implicit
  ec: ExecutionContext
) extends InjectedController {

  def webhook(clientId: String, integrationId: String) =
    Action.async(unicodeTextParser) { implicit req =>
      implicit val config: LineConfig = LineConfig.fromEnv

      if (validator.validate(req.headers(config.SignatureHeaderName), req.body)) {
        val parsed = Json.parse(req.body).as[Request]
        val replyIter = handler.handle(parsed)
        Future.sequence(replyIter.map(lineClient.reply)).map(_ => Ok)
      } else
        Future.successful(Ok)
    }

  private[this] def unicodeTextParser =
    parse.raw.map(_.asBytes(parse.DefaultMaxDiskLength).fold("")(_.decodeString(StandardCharsets.UTF_8)))

}
