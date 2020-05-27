import javax.inject._

import akka.stream.Materializer
import play.api.http.DefaultHttpFilters
import play.api.mvc.{Filter, RequestHeader, Result}
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Filters @Inject() (gzip: GzipFilter, cors: CORSFilter, pcors: ProactiveCorsFilter)
  extends DefaultHttpFilters(pcors, cors, gzip)

// CordvaやElectronなどでは `Origin: file://` というヘッダーが投げられるがPlayのデフォルトCORSヘッダではこれを拒否してしまうためプレフィルターを使ってごまかす
private class ProactiveCorsFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader) =
    rh.headers.get("Origin").getOrElse("") match {
      case origin if origin.contains("file://") =>
        f(rh.withHeaders(rh.headers.replace("Origin" -> "https://dummy.sugures.app")))
          .map(_.withHeaders("Access-Control-Allow-Origin" -> origin))
      case _ =>
        f(rh)
    }
}
