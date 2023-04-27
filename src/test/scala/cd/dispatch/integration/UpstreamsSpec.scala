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

object UpstreamsSpec extends ZIOSpecDefault {

  def appZio = for {
    upstreamApp <- ZIO.serviceWith[UpstreamController](_.create())
  } yield upstreamApp

  def spec = suite("upstream endpoints")(
    test("should respond on a hart beat") {
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
        app <- appZio
        server <- Server.start(testPort, app).fork
        response <- request
        _ <- server.interrupt
        body <- response.bodyAsString
      } yield  {
        assertTrue(response.headers == expectedResp.headers)
        assertTrue(response.status == expectedResp.status)
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
       app <- appZio
       call <- Server.start(testPort, app).fork
       response <- Client.request(s"http://$testHost:$testPort/app.clearscore.com/api/global/backend-tech-test/v1/cards")
       _ <- call.interrupt
       body <- response.bodyAsString
     } yield {
       assertTrue(response.headers == expectedResp.headers)
       assertTrue(response.status == expectedResp.status)
       assertTrue(body == csCardsResponse.stripMargin)
     }
    },

    test("should return Scored Cards request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(("content-type", "application/json"), ("content-length", "207")),
        data = HttpData.fromString(scoredCardsResponse.stripMargin)
      )

      for {
        app <- appZio
        call <- Server.start(testPort, app).fork
        response <- Client.request(s"http://$testHost:$testPort/app.clearscore.com/api/global/backend-tech-test/v2/creditcards")
        _ <- call.interrupt
        body <- response.bodyAsString
      } yield {
        assertTrue(response.headers == expectedResp.headers)
        assertTrue(response.status == expectedResp.status)
        assertTrue(body == scoredCardsResponse.stripMargin)
      }
    },

    test("should return xx Scored Cards request") {
      for {
        app <- appZio
        call <- Server.start(testPort, app).fork
        response <- Client.request(s"http://$testHost:$testPort/not-exist")
        _ <- call.interrupt
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
