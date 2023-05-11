package cs.dispatch.integration

import cs.dispatch.util.TestHelper.*
import cs.dispatch.config.{AppConfig, Config, UpstreamResponseConfig}
import cs.dispatch.servers.controllers.{
  RecommendationController,
  UpstreamController
}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import io.netty.util.AsciiString
import org.mockserver.client.MockServerClient
import zio.http.*
import zio.{UIO, json, *}
import zio.test.TestAspect.{flaky, forked, retries, sequential, timeout}
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import sttp.model.HeaderNames

object CSCardsTimeoutSpec extends ZIOSpecDefault {

  val mockServer = ClientAndServer.startClientAndServer(9001)
  val mockClient = new MockServerClient("localhost", mockServer.getPort)

  mockClient
    .when(
      HttpRequest
        .request()
        .withPath("/app.clearscore.com/api/global/backend-tech-test/v1/cards")
    )
    .respond(HttpResponse.response().withStatusCode(504))

  mockClient
    .when(
      HttpRequest
        .request()
        .withPath(
          "/app.clearscore.com/api/global/backend-tech-test/v2/creditcards"
        )
    )
    .respond(HttpResponse.response().withBody(scoredCardsResponse))

  def spec = suite("CSCards timeout")(
    test("should return valid response with only Scored Card items") {

      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers("content-type", "application/json"),
        body = Body.fromString(csCardsResponse)
      )

      val request = Request(
        url = URL(!! / "creditcards"),
        method = Method.POST,
        body = Body.fromString(testUser),
        headers = Headers.empty,
        version = Version.Http_1_1,
        remoteAddress = None
      )

      for {
        app <- ZIO.serviceWith[RecommendationController](_.create())
        response <- app.runZIO(request)
        body <- response.body.asString
      } yield {
        val bodyStrip = body.replaceAll(" ", "")
        val equalsStrip =
          testResponseWithoutCSCards.replaceAll("\n", "").replaceAll(" ", "")
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(bodyStrip.equals(equalsStrip))
      }
    }
  ).provide(
    ZLayer.succeed(appConfig.copy(zioHttp = zioHttpConfig.copy(port = 9001))),
    RecommendationService.live,
    RecommendationController.live
  )
}
