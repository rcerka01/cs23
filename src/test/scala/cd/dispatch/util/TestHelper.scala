package cd.dispatch.util

import cs.dispatch.config.{AppConfig, Call, UpstreamResponseConfig, ZioHttpConfig}
import zhttp.http.{HeaderNames, HeaderValues, Headers}
import zio.*

object TestHelper {
  val testHost = "127.0.0.1"
  val testPort = 3333
  
  val csCardsResponse =
    """
      |[
      |    {
      |        "apr": 21.4c,
      |        "cardName": "SuperSaver Card",
      |        "eligibility": 6.3
      |    },
      |    {
      |        "apr": 19.2,
      |        "cardName": "SuperSpender Card",
      |        "eligibility": 5.0
      |    }
      |]
      |"""
    
  val scoredCardsResponse =
    """
      |[
      |    {
      |       "card": "ScoredCard Builder",
      |       "apr": 19.4,
      |       "approvalRating": 0.8
      |    }
      |]
      |"""

  val zioHttpConfig: ZioHttpConfig = ZioHttpConfig(testHost, testPort)
  val call1: Call = Call("cards", "CSCards", "/app.clearscore.com/api/global/backend-tech-test/v1/cards", 1.seconds, csCardsResponse)
  val call2: Call = Call("creditcards", "ScoredCards", "/app.clearscore.com/api/global/backend-tech-test/v2/creditcards", 1.seconds, scoredCardsResponse)
  val upstreamResponseConfig: UpstreamResponseConfig = UpstreamResponseConfig(List(call1, call2))
  val appConfig: AppConfig = AppConfig(zioHttpConfig, upstreamResponseConfig)
}
