package cs.dispatch.integration

import cs.dispatch.config.{AppConfig, Config}
import cs.dispatch.controllers.{RecommendationController, UpstreamController}
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
import zio.http.{!!, Body, Header, Headers, Method, Request, Response, Status, URL, Version}
import zio.*
import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}
import cs.dispatch.util.TestHelper.*
import io.netty.util.AsciiString
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.{HttpRequest, HttpResponse}
import zio.test.TestAspect.{flaky, forked, retries, sequential, timeout}

object RecommendationsSpec extends ZIOSpecDefault {

  val mockServer = ClientAndServer.startClientAndServer(9000)
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
    .respond(HttpResponse.response().withBody(scoredCardsResponse))

  def spec = suite("recommendations endpoint")(
    test("should return valid request") {
      val expectedResp = Response(
        status = Status.Ok,
        headers = Headers(
          List(
            Header.ContentLength(241),
            Header.Custom("Content-Type", "application/json")
          )
        ),        body = Body.fromString(testResponse)
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
        recommendationApp <- ZIO.serviceWith[RecommendationController](
          _.create()
        )
        response <- recommendationApp.runZIO(request)
        body <- response.body.asString
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
    RecommendationService.live,
    RecommendationController.live
  )
}
