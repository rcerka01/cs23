package cs.dispatch.util

import cs.dispatch.config.{
  AppConfig,
  Call,
  UpstreamResponseConfig,
  ZioHttpConfig,
  OpenApi
}
import cs.dispatch.controllers.{RecommendationController, UpstreamController}
import cs.dispatch.services.CallType
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

  val testHost = "localhost"
  val testPort = 9000
  val zioHttpConfig: ZioHttpConfig = ZioHttpConfig(testHost, testPort)
  val call1: Call = Call(
    CallType.Cards,
    "CSCards",
    "/app.clearscore.com/api/global/backend-tech-test/v1/cards",
    1.seconds,
    csCardsResponse
  )
  val call2: Call = Call(
    CallType.CreditCards,
    "ScoredCards",
    "/app.clearscore.com/api/global/backend-tech-test/v2/creditcards",
    1.seconds,
    scoredCardsResponse
  )
  val upstreamResponseConfig: UpstreamResponseConfig = UpstreamResponseConfig(
    List(call1, call2)
  )
  val appConfig: AppConfig = AppConfig(
    zioHttpConfig,
    upstreamResponseConfig,
    OpenApi("test", "1.0", "test.yaml")
  )
}
