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
import zio.test.TestAspect.{flaky, forked, retries, sequential, timeout}

object RecommendationsSpec extends ZIOSpecDefault {
  def spec = suite("recommendations endpoint")(
    test("should return valid request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(
          ("content-type", "application/json"),
          ("content-length", "241")
        ),
        data = HttpData.fromString(testResponse)
      )

      val request = Client.request(
        url = s"http://$testHost:$testPort/creditcards",
        method = Method.POST,
        content = HttpData.fromString(testUser)
      )

      for {
        response <- testServer(request)
        body <- response.bodyAsString
      } yield {
        val bodyStrip = body.replaceAll(" ", "")
        val equalsStrip =
          testResponse.replaceAll("\n", "").replaceAll(" ", "")
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(bodyStrip == equalsStrip)
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
