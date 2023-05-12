package cs.dispatch.integration

import cs.dispatch.util.TestHelper.*
import cs.dispatch.config.{AppConfig, Config}
import cs.dispatch.controllers.{RecommendationController, UpstreamController}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import io.netty.util.AsciiString
import zio.*
import zio.http.{!!, Body, Headers, Method, Request, Status, URL, Version}
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
        body = Body.fromString("invalid_user_data"),
        headers = Headers.empty,
        version = Version.Http_1_1,
        remoteAddress = None
      )

      for {
        app <- appZio
        response <- app.runZIO(request)
      } yield assertTrue(response.status == Status.BadRequest)
    }
  ).provide(
    ZLayer.succeed(appConfig),
    UpstreamImitatorService.live,
    UpstreamController.live,
    RecommendationService.live,
    RecommendationController.live
  ) @@ sequential
}
