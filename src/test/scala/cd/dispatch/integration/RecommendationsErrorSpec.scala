package cd.dispatch.integration

import cd.dispatch.util.TestHelper.*
import cs.dispatch.Context
import cs.dispatch.Context.Env
import cs.dispatch.config.{AppConfig, Config}
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

object RecommendationsErrorSpec extends ZIOSpecDefault {

  val appZio = for {
    upstreamApp <- ZIO.serviceWith[UpstreamController](_.create())
    recommendationApp <- ZIO.serviceWith[RecommendationController](_.create())
  } yield upstreamApp ++ recommendationApp

  def spec = suite("recommendations endpoint on errors")(
    test("should return bad response if can't serialize User") {
      for {
        app <- appZio
        fiber <- Server.start(testPort, app).forever.fork
        response <- Client.request(
          url = s"http://$testHost:$testPort/creditcards",
          method = Method.POST,
          content = HttpData.fromString("invalid_user")
        )
        _ <- fiber.interrupt
      } yield {
        assertTrue(response.status == Status.BadRequest)
      }
    },
    test("should return status Not Found") {
      for {
        app <- appZio
        fiber <- Server.start(testPort, app).fork
        response <- Client.request(
          s"http://$testHost:$testPort/not-exist"
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
