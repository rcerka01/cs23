package cd.dispatch.integration

import cd.dispatch.util.TestHelper.*
import cs.dispatch.Context
import cs.dispatch.Context.Env
import cs.dispatch.config.{AppConfig, Config, UpstreamResponseConfig}
import cs.dispatch.servers.controllers.{RecommendationController, UpstreamController}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import io.netty.util.AsciiString
import zhttp.http.*
import zhttp.service.{Client, Server}
import zio.*
import zio.test.TestAspect.sequential
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}

object RecommendationsCSCardsTimeoutSpec extends ZIOSpecDefault {
  val recommendationsTestPort = appConfig.zioHttp.port
  val recommendationsTestHost = appConfig.zioHttp.host

  val cSCardRespnseWithTimeout = call1.copy(timeout = 500.millis)
  val upstreamResponseConfigWithCSCardTimeout: UpstreamResponseConfig = UpstreamResponseConfig(List(cSCardRespnseWithTimeout, call2))
  val appConfigWithCSCardTimeout: AppConfig = appConfig.copy(upstreamResponse = upstreamResponseConfigWithCSCardTimeout)

  def serverZio = for {
    upstreamApp <- ZIO.serviceWith[UpstreamController](_.create())
    recommendationApp <- ZIO.serviceWith[RecommendationController](_.create())
  } yield Server.start(recommendationsTestPort, upstreamApp ++ recommendationApp)

  def spec = suite("recommendations endpoint with CSCards timeout")(
    test ("should return valid response with only Scored Card items") {
      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(("content-type", "application/json"),("content-length", "85")),
        data = HttpData.fromString(csCardsResponse)
      )

      for {
        server <- serverZio
        fiber <- server.fork
        response <- Client.request(
          url = s"http://$recommendationsTestHost:$recommendationsTestPort/creditcards",
          method = Method.POST,
          content = HttpData.fromString(testUser)
        )
        body <- response.bodyAsString
        _ <- fiber.interrupt
      } yield {
         val bodyStrip = body.replaceAll(" ", "")
         val equalsStrip = testResponseWithoutCSCards.replaceAll("\n","").replaceAll(" ", "")
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(bodyStrip.equals(equalsStrip))
      }
    },
  ).provide (
    ZLayer.succeed(appConfigWithCSCardTimeout),
    Context.live,
    UpstreamImitatorService.live,
    UpstreamController.live,
    RecommendationService.live,
    RecommendationController.live,
  )
}
