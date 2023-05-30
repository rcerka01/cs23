package cs.dispatch.integration

import cs.dispatch.util.TestHelper.*
import cs.dispatch.config.{AppConfig, Config, UpstreamResponseConfig}
import cs.dispatch.controllers.{RecommendationController, UpstreamController}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import io.netty.util.AsciiString
import org.mockserver.client.MockServerClient
import zio.http.*
import zio.*
import zio.test.TestAspect.{flaky, forked, retries, sequential, timeout}
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import sttp.model.HeaderNames.ContentLength
import zio.http.Header.Custom

object ScoredCardsTimeoutSpec extends ZIOSpecDefault {

  val mockServer = ClientAndServer.startClientAndServer(9002)
  val mockClient = new MockServerClient("localhost", mockServer.getPort)

  mockClient
    .when(
      HttpRequest
        .request()
        .withPath("/app.clearscore.com/api/global/backend-tech-test/v1/cards")
    )
    .respond(HttpResponse.response().withBody(csCardsResponse))

  mockClient
    .when(
      HttpRequest
        .request()
        .withPath(
          "/app.clearscore.com/api/global/backend-tech-test/v2/creditcards"
        )
    )
    .respond(HttpResponse.response().withStatusCode(504))

  def spec = suite("Scored Cards timeout")(
    test("should return valid response with only CSCards items") {

      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(
          List(
            Header.ContentLength(157),
            Header.Custom("Content-Type", "application/json")
          )
        ),
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
          testResponseWithoutScoredCards
            .replaceAll("\n", "")
            .replaceAll(" ", "")
        assertTrue(response.status == expectedResp.status) &&
        assertTrue(response.headers == expectedResp.headers) &&
        assertTrue(bodyStrip.equals(equalsStrip))
      }
    }
  ).provide(
    ZLayer.succeed(appConfig.copy(httpPort= 9002)),
    RecommendationService.live,
    RecommendationController.live
  )
}
