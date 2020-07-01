import javax.servlet.ServletContext

import akka.actor.ActorSystem
//import akka.stream.{Materializer, SystemMaterializer}
import akka.stream.{ActorMaterializer, Materializer}
import controllers.{HealthCheckController, LineController}
import infrastructure.LineClient
import org.scalatra.LifeCycle
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import service.WebhookHandler

class ScalatraBootstrap extends LifeCycle {

  implicit val system: ActorSystem = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }

//  implicit val materializer: Materializer = SystemMaterializer(system).materializer
  implicit val materializer: Materializer = ActorMaterializer()

  val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()

  override def init(context: ServletContext) {
    val lineClient = new LineClient(wsClient)
    val webhookHandler = new WebhookHandler

    context.mount(new HealthCheckController(system), "/status")
    context.mount(new LineController(system, lineClient, webhookHandler), "/webhook")
  }

  override def destroy(context: ServletContext) {
    wsClient.close()
    system.terminate()
  }
}
