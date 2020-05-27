import javax.servlet.ServletContext

import akka.actor.{ActorSystem, Props}
import controllers.{HealthCheckController, LineController}
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {

  val system = ActorSystem()

  override def init(context: ServletContext) {
    context.mount(new HealthCheckController(system), "/status")
    context.mount(new LineController(system), "/webhook")
  }

  override def destroy(context: ServletContext) {
    system.terminate()
  }
}
