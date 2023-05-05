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
import zhttp.service.{ChannelFactory, Client, EventLoopGroup, Server}
import zio.*
import zio.test.TestAspect.{aroundTest, flaky, sequential, success, timeout}
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}

object RecommendationsErrorSpec extends ZIOSpecDefault {

  val appZio = for {
    upstreamApp <- ZIO.serviceWith[UpstreamController](_.create())
    recommendationApp <- ZIO.serviceWith[RecommendationController](_.create())
  } yield upstreamApp ++ recommendationApp

  def spec = suite("recommendations endpoint on errors")(
    test("should return bad response if can't serialize User") {
      val request = Request(
        url = URL(!! / "creditcards"),
        method = Method.POST,
        data = HttpData.fromString("invalid_user_data")
      )

      for {
        app <- appZio
        response <- app(request)
      } yield assertTrue(response.status == Status.BadRequest)
    }
  ).provide(
    ZLayer.succeed(appConfig),
    Context.live,
    UpstreamImitatorService.live,
    UpstreamController.live,
    RecommendationService.live,
    RecommendationController.live
  ) @@ sequential
}
