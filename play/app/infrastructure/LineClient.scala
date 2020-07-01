package infrastructure

import javax.inject.Inject

import io.LineConfig
import io.reply._
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class LineClient @Inject() (
  ws: WSClient
) {
  def reply(request: Request)(implicit
    ec: ExecutionContext,
    lineConfig: LineConfig
  ): Future[WSResponse] =
    ws.url(lineConfig.replyApiEndpoint)
      .withHttpHeaders(
        "Authorization" -> s"Bearer ${lineConfig.channelAccessToken}",
        "Content-Type" -> "application/json"
      )
      .post(Json.toJson(request))
}
