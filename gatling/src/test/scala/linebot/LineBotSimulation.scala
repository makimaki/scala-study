package linebot

import java.nio.charset.StandardCharsets
import java.util.{Base64, UUID}

import scala.concurrent.duration._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import play.api.libs.json.{JsValue, Json}

class LineBotSimulation extends Simulation {

  import LineBotSimulation._

  val target = sys.env("TARGET")
  val baseUri = target match {
    case "golang"          => "http://localhost:8080"
    case "rust"            => "http://localhost:3000"
    case "scala(play)"     => "http://localhost:9000"
    case "scala(scalatra)" => "http://localhost:8080"
  }

  val httpProtocol = http
    .baseUrl(baseUri)
    .acceptHeader("""text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""")
    .acceptEncodingHeader("""gzip, deflate""")
    .acceptLanguageHeader("""en-gb,en;q=0.5""")
    .userAgentHeader("""Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:31.0) Gecko/20100101 Firefox/31.0""")
    .proxy(Proxy("10.63.0.11", 8080))
    .noProxyFor("localhost", "127.0.0.1", "::1", "10.0.0.0/8")

  val allCheckScenario = scenario("All Check")
    .exec(
      _.setAll(
        "id" -> UUID.randomUUID.toString,
        "target" -> target
      )
    )
    //    .exec {
    //      http("healthy")
    //        .get("/status")
    //        .check(status.is(200))
    //    }
    .exec { session =>
      val body = textMessage(session("id").as[String], "あなたは誰ですか？")
      session.setAll(
        "body" -> body,
        "signature" -> sign(body)
      )
    }
    .exec {
      webhook("who are you")
      //    }
      //    .exec {
      //      replyCheck("who are you reply")
        .check(
          status.is(200),
          jsonPath("$.messages[0].text").is("私は ${target} で実装された何かです。")
        )
    }
    .exec { session =>
      val body = textMessage(session("id").as[String], "発射！")
      session.setAll(
        "body" -> body,
        "signature" -> sign(body)
      )
    }
    .exec {
      webhook("parrot")
      //    }
      //    .exec {
      //      replyCheck("parrot reply")
        .check(
          status.is(200),
          jsonPath("$.messages[0].text").is("発射！ ですね。わかります。")
        )
    }
    .exec { session =>
      val body = locationMessage(
        session("id").as[String],
        "カイルア・ビーチ・パーク ®",
        21.391927,
        -157.7530065
      )
      session.setAll(
        "body" -> body,
        "signature" -> sign(body)
      )
    }
    .exec {
      webhook("send location")
      //    }
      //    .exec {
      //      replyCheck("send location reply")
        .check(
          status.is(200),
          jsonPath("$.messages[0].template.text").is("カイルア・ビーチ・パーク ® ですね。Google Maps で開きたいですか？"),
          jsonPath("$.messages[0].template.actions[0].data").saveAs("postback")
        )
    }
    .exec { session =>
      val body = postback(session("id").as[String], session("postback").as[String])
      session.setAll(
        "body" -> body,
        "signature" -> sign(body)
      )
    }
    .exec {
      webhook("send postback")
      //    }
      //    .exec {
      //      replyCheck("send location reply")
        .check(
          status.is(200),
          jsonPath("$.messages[0].altText").is("これはだいたいてきすと"),
          jsonPath("$.messages[0].template.actions[0].uri").is("${postback}")
        )
    }

  setUp(
    allCheckScenario
      .inject(
        heavisideUsers(sys.env("AMMUNITION").toInt).during(1.minute)
//        atOnceUsers(1)
      )
      .protocols(httpProtocol)
  )

  def webhook(requestName: String) =
    http(requestName)
      .post("/webhook/00000000-0000-0000-0000-000000000000/test-integration")
      .headers(
        Map(
          "Content-Type" -> "application/json",
          "X-Line-Signature" -> "${signature}"
        )
      )
      .body(StringBody("${body}"))
      .check(status.is(200))

//  def replyCheck(requestName: String) =
//    http(requestName).get(s"${sys.env("LINE_REPLY_API_ENDPOINT")}/last/$${id}")
}

object LineBotSimulation {
  def sign(body: String): String = {
    val SignatureAlgorithm = "HmacSHA256"

    val mac = Mac.getInstance(SignatureAlgorithm)

    mac.init(new SecretKeySpec(sys.env("LINE_CHANNEL_SECRET").getBytes, SignatureAlgorithm))

    val hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8))

    Base64.getEncoder.encodeToString(hash)
  }

  private[this] def createRequest(id: String, eventType: String, value: JsValue) =
    Json.obj(
      "events" -> Json.arr(
        Json.obj(
          "replyToken" -> id,
          "type" -> eventType,
          "timestamp" -> System.currentTimeMillis,
          "source" -> Json.obj(
            "type" -> "user",
            "userId" -> "Gatling"
          ),
          eventType -> value
        )
      )
    )

  def textMessage(id: String, text: String): String =
    createRequest(
      id,
      "message",
      Json.obj(
        "id" -> UUID.randomUUID.toString,
        "type" -> "text",
        "text" -> text
      )
    ).toString

  def locationMessage(id: String, title: String, latitude: Double, longitude: Double): String =
    createRequest(
      id,
      "message",
      Json.obj(
        "id" -> UUID.randomUUID.toString,
        "type" -> "location",
        "title" -> title,
        "latitude" -> latitude,
        "longitude" -> longitude
      )
    ).toString

  def postback(id: String, data: String): String =
    createRequest(
      id,
      "postback",
      Json.obj(
        "data" -> data
      )
    ).toString
}
