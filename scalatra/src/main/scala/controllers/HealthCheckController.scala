package controllers

import akka.actor.ActorSystem
import org.scalatra.{AsyncResult, FutureSupport, Ok, ScalatraServlet}

import scala.concurrent.{ExecutionContext, Future}

class HealthCheckController(system: ActorSystem) extends ScalatraServlet with FutureSupport {

  protected implicit def executor: ExecutionContext = system.dispatcher

  get("/") {
    new AsyncResult {
      val is =
        Future.successful(Ok())
    }
  }

}
