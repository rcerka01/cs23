package cd.dispatch.integration

import cs.dispatch.Context
import cs.dispatch.config.{AppConfig, Config}
import cs.dispatch.servers.controllers.{
  RecommendationController,
  UpstreamController
}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import zhttp.http.{
  HeaderNames,
  HeaderValues,
  Headers,
  HttpData,
  Method,
  Request,
  Response,
  Status
}
import zio.*
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}
import cd.dispatch.util.TestHelper.*
import cs.dispatch.Context.Env
import io.netty.util.AsciiString
import zhttp.service.{Client, Server}
import zio.test.TestAspect.sequential

object RecommendationsSpec extends ZIOSpecDefault {

  val recommendationsTestPort = appConfig.zioHttp.port
  val recommendationsTestHost = appConfig.zioHttp.host

  def serverZio = for {
    upstreamApp <- ZIO.serviceWith[UpstreamController](_.create())
    recommendationApp <- ZIO.serviceWith[RecommendationController](_.create())
  } yield Server.start(
    recommendationsTestPort,
    upstreamApp ++ recommendationApp
  )

  def spec = suite("recommendations endpoint")(
    test("should return valid request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(
          ("content-type", "application/json"),
          ("content-length", "241")
        ),
        data = HttpData.fromString(csCardsResponse)
      )

      for {
        server <- serverZio
        fiber <- server.fork
        response <- Client.request(
          url =
            s"http://$recommendationsTestHost:$recommendationsTestPort/creditcards",
          method = Method.POST,
          content = HttpData.fromString(testUser)
        )
        body <- response.bodyAsString
        _ <- fiber.interrupt
      } yield {
        val bodyStrip = body.replaceAll(" ", "")
        val equalsStrip = testResponse.replaceAll("\n", "").replaceAll(" ", "")
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(bodyStrip.equals(equalsStrip))
      }
    },
    test("should return bad response if can't serialize User") {
      for {
        server <- serverZio
        fiber <- server.fork
        response <- Client.request(
          url =
            s"http://$recommendationsTestHost:$recommendationsTestPort/creditcards",
          method = Method.POST,
          content = HttpData.fromString("invalid_user")
        )
        body <- response.bodyAsString
        _ <- fiber.interrupt
      } yield {
        assertTrue(response.status == Status.BadRequest)
      }
    },
    test("should return status Not Found") {
      for {
        server <- serverZio
        fiber <- server.fork
        response <- Client.request(
          s"http://$recommendationsTestHost:$recommendationsTestPort/not-exist"
        )
        _ <- fiber.interrupt
      } yield {
        assertTrue(response.status == Status.NotFound)
      }
    }
  ).provide(
    ZLayer.succeed(appConfig),
    Context.live,
    UpstreamImitatorService.live,
    UpstreamController.live,
    RecommendationService.live,
    RecommendationController.live
  )
}
