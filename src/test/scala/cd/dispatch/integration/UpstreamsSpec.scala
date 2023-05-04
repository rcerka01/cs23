package cd.dispatch.integration

import cs.dispatch.Context
import cs.dispatch.config.{AppConfig, Config}
import cs.dispatch.servers.controllers.{
  RecommendationController,
  UpstreamController
}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import zhttp.http.{
  !!,
  HeaderNames,
  HeaderValues,
  Headers,
  HttpData,
  Method,
  Request,
  Response,
  Status,
  URL
}
import zio.*
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}
import cd.dispatch.util.TestHelper.*
import cs.dispatch.Context.Env
import io.netty.util.AsciiString
import zhttp.service.{Client, Server}
import zio.test.TestAspect.{sequential, timeout}

object UpstreamsSpec extends ZIOSpecDefault {

  def appZio = for {
    upstreamApp <- ZIO.serviceWith[UpstreamController](_.create())
  } yield upstreamApp

  def spec = suite("upstream endpoints")(
    test("should respond on a heart beat") {
      val data = """{"hallo": ok}"""

      val request = Request(
        url = URL(!! / "test"),
        method = Method.POST,
        data = HttpData.fromString(data)
      )

      val expectedResp = Response(
        status = Status.Ok,
        headers =
          Headers(HeaderNames.contentType, HeaderValues.applicationJson),
        data = HttpData.fromString(data)
      )

      for {
        app <- appZio
        response <- app(request)
        body <- response.bodyAsString
      } yield {
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(body == data)
      }
    },
    test("should return CSCards request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers =
          Headers(HeaderNames.contentType, HeaderValues.applicationJson),
        data = HttpData.fromString(csCardsResponse)
      )

      val request = Request(
        url = URL(
          !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v1" / "cards"
        ),
        method = Method.GET,
        data = HttpData.empty
      )

      for {
        app <- appZio
        response <- app(request)
        body <- response.bodyAsString
      } yield {
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(body == csCardsResponse)
      }
    },
    test("should return Scored Cards request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers =
          Headers(HeaderNames.contentType, HeaderValues.applicationJson),
        data = HttpData.fromString(scoredCardsResponse)
      )

      val request = Request(
        url = URL(
          !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v2" / "creditcards"
        ),
        method = Method.GET,
        data = HttpData.empty
      )

      for {
        app <- appZio
        response <- app(request)
        body <- response.bodyAsString
      } yield {
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(body == scoredCardsResponse)
      }
    },
    test("should return status Not Found") {
      for {
        app <- appZio
        fiber <- Server.start(testPort, app).fork
        response <- Client.request(s"http://127.0.0.1:$testPort/not-exist")
        _ <- fiber.interrupt
      } yield {
        assertTrue(response.status == Status.NotFound)
      }
    }
  ).provide(
    ZLayer.succeed(appConfig),
    Context.live,
    UpstreamImitatorService.live,
    UpstreamController.live
  )
}
