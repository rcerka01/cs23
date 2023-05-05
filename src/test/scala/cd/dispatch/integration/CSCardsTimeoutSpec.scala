//package cd.dispatch.integration
//
//import cd.dispatch.util.TestHelper.*
//import cs.dispatch.Context
//import cs.dispatch.Context.Env
//import cs.dispatch.config.{AppConfig, Config, UpstreamResponseConfig}
//import cs.dispatch.servers.controllers.{
//  RecommendationController,
//  UpstreamController
//}
//import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}
//import io.netty.util.AsciiString
//import zhttp.http.*
//import zhttp.service.{Client, Server}
//import zio.*
//import zio.test.TestAspect.{flaky, forked, retries, sequential, timeout}
//import zio.test.{TestAspect, TestClock, ZIOSpecDefault, assertTrue}
//import zio.json
//
//object CSCardsTimeoutSpec extends ZIOSpecDefault {
//
//  val upstreamAppZIO: Http[Any, Nothing, Request, Response] =
//    Http.collectZIO[Request] {
//      case Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v1" / "cards" =>
//        ZIO.logError("frfr") *> ZIO.succeed(Response.json(csCardsResponse))
//      //  ZIO.succeed(Response.fromHttpError(HttpError.GatewayTimeout("Timeout exception")))
//      case Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v2" / "creditcards" =>
//        ZIO.succeed(Response.json(scoredCardsResponse))
//    }
//
//  def appZio = for {
//    recommendationApp <- ZIO.serviceWith[RecommendationController](_.create())
//  } yield upstreamAppZIO ++ recommendationApp
//
//  def spec = suite("CSCards timeout")(
//    test("should return valid response with only Scored Card items") {
//
//      val expectedResp = Response(
//        status = Status.Ok,
//        headers = Headers(
//          ("content-type", "application/json"),
//          ("content-length", "85")
//        ),
//        data = HttpData.fromString(csCardsResponse)
//      )
//
//      val r = Request(
//        url = URL(!! / "creditcards"),
//        method = Method.POST,
//        data = HttpData.fromString(testUser)
//      )
//
//      ZIO.scoped {
//        for {
//          app <- appZio
//          response <- app(r)
////          fiber <- Server.start(testPort, app).fork
////          response <- Client.request(
////            url = s"http://$testHost:$testPort/creditcards",
////            method = Method.POST,
////            content = HttpData.fromString(testUser)
////          )
//          body <- response.bodyAsString
//          // _ <- fiber.interrupt
//        } yield {
//          val bodyStrip = body.replaceAll(" ", "")
//          val equalsStrip =
//            testResponseWithoutCSCards.replaceAll("\n", "").replaceAll(" ", "")
//          assertTrue(response.status == expectedResp.status) &&
//          assertTrue(response.headers == expectedResp.headers) &&
//          assertTrue(bodyStrip.equals(equalsStrip))
//        }
//      }
//    } @@ timeout(5.seconds)
//  ).provide(
//    Context.live,
//    ZLayer.succeed(appConfig),
//    RecommendationService.live,
//    RecommendationController.live
//  )
//
//}
