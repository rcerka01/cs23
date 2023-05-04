package cd.dispatch.integration

import cd.dispatch.util.TestHelper.*
import cs.dispatch.Context
import cs.dispatch.Context.Env
import cs.dispatch.config.{AppConfig, Config, UpstreamResponseConfig}
import cs.dispatch.servers.controllers.{
  RecommendationController,
  UpstreamController
}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import io.netty.util.AsciiString
import zhttp.http.*
import zhttp.service.{Client, Server}
import zio.*
import zio.test.TestAspect.{sequential, timeout}
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}

object ScoredCardsTimeoutSpec extends ZIOSpecDefault {

  val upstreamAppZIO: Http[Any, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v1" / "cards" =>
        ZIO.succeed(Response.json(csCardsResponse))
      case Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v2" / "creditcards" =>
        ZIO.succeed(Response.json(scoredCardsResponse)).delay(3.seconds)
    }

  def appZio = for {
    recommendationApp <- ZIO.serviceWith[RecommendationController](_.create())
  } yield upstreamAppZIO ++ recommendationApp

  def spec = suite("Scored Cards timeout")(
    test("should return valid response with only Scored Card items") {

      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(
          ("content-type", "application/json"),
          ("content-length", "157")
        ),
        data = HttpData.fromString(csCardsResponse)
      )

      for {
        app <- appZio
        fiber <- Server.start(testPort, app).fork
        response <- Client.request(
          url = s"http://$testHost:$testPort/creditcards",
          method = Method.POST,
          content = HttpData.fromString(testUser)
        )
        body <- response.bodyAsString
        _ <- fiber.interrupt
      } yield {
        val bodyStrip = body.replaceAll(" ", "")
        val equalsStrip =
          testResponseWithoutScoredCards
            .replaceAll("\n", "")
            .replaceAll(" ", "")
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(bodyStrip.equals(equalsStrip))
      }
    } @@ sequential
  ).provide(
    Context.live,
    ZLayer.succeed(appConfig),
    RecommendationService.live,
    RecommendationController.live
  ) @@ sequential
}
