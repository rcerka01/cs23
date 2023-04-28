package cd.dispatch.integration

import cs.dispatch.Context
import cs.dispatch.config.{AppConfig, Config}
import cs.dispatch.servers.controllers.{RecommendationController, UpstreamController}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import zhttp.http.{HeaderNames, HeaderValues, Headers, HttpData, Method, Request, Response, Status}
import zio.*
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}
import cd.dispatch.util.TestHelper.*
import cs.dispatch.Context.Env
import io.netty.util.AsciiString
import zhttp.service.{Client, Server}
import zio.test.TestAspect.sequential

object UpstreamsSpec extends ZIOSpecDefault {

  def serverZio = for {
    upstreamApp <- ZIO.serviceWith[UpstreamController](_.create())
  } yield Server.start(testPort, upstreamApp).forever

  def spec = suite("upstream endpoints")(
    test("should respond on a heart beat") {
      val data = """{"hallo": ok}"""

      val request = Client.request(
        url = s"http://$testHost:$testPort/test",
        method = Method.POST,
        content = HttpData.fromString(data),
      )

      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(("content-type", "application/json"), ("content-length", "13")),
        data = HttpData.fromString(data)
      )

      for {
        server <- serverZio
        fiber <- server.fork
        response <- request
        body <- response.bodyAsString
        _ <- fiber.interrupt
      } yield  {
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(body == data)
      }
    },

    test ("should return CSCards request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(("content-type", "application/json"),("content-length", "207")),
        data = HttpData.fromString(csCardsResponse.stripMargin)
      )

     for {
       server <- serverZio
       fiber <- server.fork
       response <- Client.request(s"http://$testHost:$testPort/app.clearscore.com/api/global/backend-tech-test/v1/cards")
       body <- response.bodyAsString
       _ <- fiber.interrupt
     } yield {
       assertTrue(response.headers == expectedResp.headers) &&
       assertTrue(response.status == expectedResp.status) &&
       assertTrue(body == csCardsResponse.stripMargin)
     }
    },

    test("should return Scored Cards request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(("content-type", "application/json"), ("content-length", "103")),
        data = HttpData.fromString(scoredCardsResponse.stripMargin)
      )

      for {
        server <- serverZio
        fiber <- server.fork
        response <- Client.request(s"http://$testHost:$testPort/app.clearscore.com/api/global/backend-tech-test/v2/creditcards")
        body <- response.bodyAsString
        _ <- fiber.interrupt
      } yield {
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(body == scoredCardsResponse.stripMargin)
      }
    },

    test("should return status Not Found") {
      for {
        server <- serverZio
        fiber <- server.fork
        response <- Client.request(s"http://$testHost:$testPort/not-exist")
        _ <- fiber.interrupt
      } yield {
        assertTrue(response.status == Status.NotFound)
      }
    }
  ).provide (
      ZLayer.succeed(appConfig),
      Context.live,
      UpstreamImitatorService.live,
      UpstreamController.live,
      RecommendationService.live
  )
}
