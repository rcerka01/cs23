package cd.dispatch.util

import cs.dispatch.config.{
  AppConfig,
  Call,
  UpstreamResponseConfig,
  ZioHttpConfig
}
import cs.dispatch.servers.controllers.{
  RecommendationController,
  UpstreamController
}
import zhttp.http.{HeaderNames, HeaderValues, Headers, Response}
import zhttp.service.{ChannelFactory, EventLoopGroup, Server}
import zio.*

object TestHelper {

  val csCardsResponse =
    """
      |[
      |    {
      |        "apr": 21.4,
      |        "cardName": "SuperSaver Card",
      |        "eligibility": 6.3
      |    },
      |    {
      |        "apr": 19.2,
      |        "cardName": "SuperSpender Card",
      |        "eligibility": 5.0
      |    }
      |]
      |""".stripMargin

  val scoredCardsResponse =
    """
      |[
      |    {
      |       "card": "ScoredCard Builder",
      |       "apr": 19.4,
      |       "approvalRating": 0.8
      |    }
      |]
      |""".stripMargin

  val testUser =
    """
      |{
      | "name": "John Smith",
      | "creditScore": 500,
      | "salary": 28000
      |}
      |""".stripMargin

  val testResponse =
    """
      |[
      |    {
      |        "provider": "ScoredCards",
      |        "name": "ScoredCard Builder",
      |        "apr": 19.4,
      |        "cardScore": 0.212
      |    },
      |    {
      |        "provider": "CSCards",
      |        "name": "SuperSaver Card",
      |        "apr": 21.4,
      |        "cardScore": 0.137
      |    },
      |    {
      |        "provider": "CSCards",
      |        "name": "SuperSpender Card",
      |        "apr": 19.2,
      |        "cardScore": 0.135
      |    }
      |]
      |""".stripMargin

  val testResponseWithoutCSCards =
    """
      |[
      |    {
      |        "provider": "ScoredCards",
      |        "name": "ScoredCard Builder",
      |        "apr": 19.4,
      |        "cardScore": 0.212
      |    }
      |]
      |""".stripMargin

  val testResponseWithoutScoredCards =
    """
      |[
      |    {
      |        "provider": "CSCards",
      |        "name": "SuperSaver Card",
      |        "apr": 21.4,
      |        "cardScore": 0.137
      |    },
      |    {
      |        "provider": "CSCards",
      |        "name": "SuperSpender Card",
      |        "apr": 19.2,
      |        "cardScore": 0.135
      |    }
      |]
      |""".stripMargin

  val testHost = "127.0.0.1"
  val testPort = 8080
  val zioHttpConfig: ZioHttpConfig = ZioHttpConfig(testHost, testPort)
  val call1: Call = Call(
    "cards",
    "CSCards",
    "/app.clearscore.com/api/global/backend-tech-test/v1/cards",
    1.seconds,
    csCardsResponse
  )
  val call2: Call = Call(
    "creditcards",
    "ScoredCards",
    "/app.clearscore.com/api/global/backend-tech-test/v2/creditcards",
    1.seconds,
    scoredCardsResponse
  )
  val upstreamResponseConfig: UpstreamResponseConfig = UpstreamResponseConfig(
    List(call1, call2)
  )
  val appConfig: AppConfig = AppConfig(zioHttpConfig, upstreamResponseConfig)

  def testServer(
      request: ZIO[EventLoopGroup & ChannelFactory, Throwable, Response]
  ) =
    ZIO.scoped {
      for {
        upstreamApp <- ZIO.serviceWith[UpstreamController](_.create())
        recommendationApp <- ZIO.serviceWith[RecommendationController](
          _.create()
        )
        server = Server.start(testPort, upstreamApp ++ recommendationApp)
        fiber <- server.fork
        response <- request
        _ <- fiber.interrupt
      } yield response
    }
}
