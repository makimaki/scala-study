import javax.servlet.ServletContext

import akka.actor.ActorSystem
import akka.stream.{Materializer, SystemMaterializer}
import controllers.{HealthCheckController, LineController}
import infrastructure.LineClient
import org.scalatra.LifeCycle
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import service.WebhookHandler

class ScalatraBootstrap extends LifeCycle {

  val system: ActorSystem = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }

  implicit val materializer: Materializer = SystemMaterializer(system).materializer

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
