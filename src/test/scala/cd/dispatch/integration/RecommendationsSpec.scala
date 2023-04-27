//package cd.dispatch.integration
//
//import cs.dispatch.Context
//import cs.dispatch.Context.Env
//import cs.dispatch.config.{Config, *}
//import cs.dispatch.servers.HttpServer
//import cs.dispatch.servers.controllers.{RecommendationController, UpstreamController}
//import cs.dispatch.services.{CallType, RecommendationService, UpstreamImitatorService}
//import zhttp.http.Path.Segment
//import zhttp.http.Path.Segment.Root
//import zhttp.http.*
//import zhttp.service.*
//import zio.*
//import zio.Config.uri
//import zio.Console.printLine
//import zio.json.*
//import zio.test.Assertion.*
//import zio.test.TestAspect.timeout
//import zio.test.*
//
//import java.time.Instant
//
//object RecommendationsSpec extends ZIOSpecDefault {
//
//  val response = """
//                  |[
//                  |    {
//                  |        "apr": 21.4,
//                  |        "cardName": "SuperSaver Card",
//                  |        "eligibility": 6.3
//                  |    },
//                  |    {
//                  |        "apr": 19.2,
//                  |        "cardName": "SuperSpender Card",
//                  |        "eligibility": 5.0
//                  |    }
//                  |]
//                  |"""
//
//  val zioHttpConfig: ZioHttpConfig = ZioHttpConfig("localhost", 8080)
//  val call: Call = Call("cards", "CSCards", "/app.clearscore.com/api/global/backend-tech-test/v1/cards", 1.seconds, response)
//  val upstreamResponseConfig: UpstreamResponseConfig = UpstreamResponseConfig(List(call))
//  val appConfig: AppConfig = AppConfig(zioHttpConfig, upstreamResponseConfig)
//
//  val contentHeaders = Headers(HeaderNames.contentType, HeaderValues.applicationJson)
//
//  val cardsRequest: Request = Request(
//    url     = URL(!! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v1" / "cards"),
//    method  = Method.GET,
//    headers = contentHeaders
//  )
//
//  def server(r: Request) = (for {
//    upstreamApp <- ZIO.serviceWith[UpstreamController](_.create())
//    recommendationsApp <- ZIO.serviceWith[RecommendationController](_.create())
//  } yield (upstreamApp ++ recommendationsApp)(r))
//    .provide(
//      Config.live,
//      UpstreamController.live,
//      RecommendationController.live,
//      UpstreamImitatorService.live,
//      RecommendationService.live
//    )
//
//  def spec = suite("http")(
//    test("should be ok") {
//      val req  = Request(method = Method.POST, url = URL(!! / "test"), data = HttpData.fromString("[]"))
//      val expectedResp = Response(status = Status.Ok, headers = contentHeaders, data = HttpData.fromString("[]"))
//      for{
//        call <- server(req)
//        response <- call
//      } yield  assertTrue(response == expectedResp)
//    },
//    test ("should be ok too") {
//      val req = Request(method = Method.POST, url = URL(!! / "test"), data = HttpData.fromString("[]"))
//      val expectedResp = Response(status = Status.Ok, headers = contentHeaders, data = HttpData.fromString("[]"))
//      for {
//        call <- server(req)
//        response <- call
//      } yield assertTrue(response == expectedResp)
//    }
//  ).provide (
//          Config.live,
//          Context.live,
//          UpstreamImitatorService.live,
//          RecommendationService.live
//  )
//}
