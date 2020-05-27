import play.api.Logging
import play.api.http.HttpErrorHandler
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler with Logging {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    logger.info(s"Client error (status: $statusCode): ${if (message.isEmpty) "(empty message)" else message}")
    Future.successful(BadRequest)
  }

  override def onServerError(request: RequestHeader, exception: Throwable) =
    exception match {
      case ex: IllegalArgumentException => Future.successful(BadRequest(ex.getMessage))
      case ex: Throwable =>
        logger.error(ex.getMessage, ex)
        Future.successful(InternalServerError)
    }
}
