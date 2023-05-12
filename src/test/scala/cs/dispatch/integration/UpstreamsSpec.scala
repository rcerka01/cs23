package cs.dispatch.integration

import cs.dispatch.config.{AppConfig, Config}
import cs.dispatch.controllers.{RecommendationController, UpstreamController}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import zio.http.{
  !!,
  Body,
  Headers,
  Method,
  Request,
  Response,
  Status,
  URL,
  Version
}
import zio.*
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}
import cs.dispatch.util.TestHelper.*
import io.netty.util.AsciiString
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
        body = Body.fromString(data),
        headers = Headers.empty,
        version = Version.Http_1_1,
        remoteAddress = None
      )

      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers("content-type", "application/json"),
        body = Body.fromString(data)
      )

      for {
        app <- appZio
        response <- app.runZIO(request)
        body <- response.body.asString
      } yield {
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(body == data)
      }
    },
    test("should return CSCards request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers("content-type", "application/json"),
        body = Body.fromString(csCardsResponse)
      )

      val request = Request(
        url = URL(
          !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v1" / "cards"
        ),
        method = Method.GET,
        body = Body.empty,
        headers = Headers.empty,
        version = Version.Http_1_1,
        remoteAddress = None
      )

      for {
        app <- appZio
        response <- app.runZIO(request)
        body <- response.body.asString
      } yield {
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(body == csCardsResponse)
      }
    },
    test("should return Scored Cards request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers("content-type", "application/json"),
        body = Body.fromString(scoredCardsResponse)
      )

      val request = Request(
        url = URL(
          !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v2" / "creditcards"
        ),
        method = Method.GET,
        body = Body.empty,
        headers = Headers.empty,
        version = Version.Http_1_1,
        remoteAddress = None
      )

      for {
        app <- appZio
        response <- app.runZIO(request)
        body <- response.body.asString
      } yield {
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(body == scoredCardsResponse)
      }
    }
  ).provide(
    ZLayer.succeed(appConfig),
    UpstreamImitatorService.live,
    UpstreamController.live
  )
}
