package controllers

import akka.actor.ActorSystem
import infrastructure.LineClient
import io.webhook._
import io.{LineConfig, Validator}
import org.scalatra._
import play.api.libs.json.Json
import service.WebhookHandler
import service.WebhookHandler.Framework

import scala.concurrent.{ExecutionContext, Future}

class LineController(system: ActorSystem, lineClient: LineClient, handler: WebhookHandler)
  extends ScalatraServlet
  with FutureSupport {

  protected implicit def executor: ExecutionContext = system.dispatcher

  post("/:clientId/:integrationId") {
    println(s"clientId: ${params("clientId")}")
    println(s"integrationId: ${params("integrationId")}")

    new AsyncResult {
      val is = {
        implicit val config: LineConfig = LineConfig.fromEnv
        implicit val framework: Framework = Framework.Scalatra

        val validator = new Validator

        if (validator.validate(request.getHeader(config.SignatureHeaderName), request.body)) {
          val parsed = Json.parse(request.body).as[Request]
          val replyIter = handler.handle(parsed)
          if (config.replyApiEndpoint == "direct")
            Future.successful(Ok(Json.toJson(replyIter.toList.head)))
          else
            Future.sequence(replyIter.map(lineClient.reply)).map(_ => Ok())
        } else
          Future.successful(Ok())
      }
    }
  }

}
