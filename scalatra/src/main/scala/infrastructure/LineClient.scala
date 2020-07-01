package infrastructure

import io.LineConfig
import io.reply._
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables._
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSRequest}

import scala.concurrent.{ExecutionContext, Future}

class LineClient(ws: StandaloneWSClient) {

  def reply(request: Request)(implicit
    ec: ExecutionContext,
    lineConfig: LineConfig
  ): Future[StandaloneWSRequest#Self#Response] =
    ws.url(lineConfig.replyApiEndpoint)
      .withHttpHeaders(
        "Authorization" -> s"Bearer ${lineConfig.channelAccessToken}",
        "Content-Type" -> "application/json"
      )
      .post(Json.toJson(request))
}
