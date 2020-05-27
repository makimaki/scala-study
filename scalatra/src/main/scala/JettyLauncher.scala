import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher {
  def main(args: Array[String]) {
    val server = new Server()
    val connector = new ServerConnector(server)

    connector.setPort(8080)

    server.setConnectors(Array(connector))

    val context = new WebAppContext()

    context.setContextPath("/")
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start
    server.join
  }
}
