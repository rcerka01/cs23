package cs.dispatch.services

import cs.dispatch.services.RecommendationServiceImpl
import zio.test.*
import zio.test.Assertion.*
import cs.dispatch.config.ZioHttpConfig
import cs.dispatch.util.TestHelper.*
import cs.dispatch.config.*
import cs.dispatch.domain.*
import cs.dispatch.util.TestHelper
import zio.*
import zio.test.*

object RecommendationsUnitSpec extends ZIOSpecDefault {

  def spec =
    suite("RecommendationServiceImpl")(
      test("getScore calculates the score correctly") {
        val recommendationServiceImpl = RecommendationServiceImpl(appConfig)

        val result1 = recommendationServiceImpl.getScore(21.4, 6.3, 0.1)
        val result2 = recommendationServiceImpl.getScore(19.2, 5, 0.1)
        val result3 = recommendationServiceImpl.getScore(19.4, 0.8, 0.01)

        assert(result1)(equalTo(0.137)) &&
        assert(result2)(equalTo(0.135)) &&
        assert(result3)(equalTo(0.212))
      },
      test("generateRecommendations returns sorted recommendations") {
        val recommendationServiceImpl = RecommendationServiceImpl(appConfig)

        val cscCardResponses = List(
          CSCardResponse(0.2, "card1", 0.6),
          CSCardResponse(0.3, "card2", 0.5),
          CSCardResponse(0.1, "card3", 0.8)
        )

        val scoredCardResponses = List(
          ScoredCardResponse("card4", 0.25, 0.4),
          ScoredCardResponse("card5", 0.35, 0.3),
          ScoredCardResponse("card6", 0.15, 0.6)
        )

        val result = recommendationServiceImpl.generateRecommendations(
          cscCardResponses,
          scoredCardResponses
        )

        assert(result.map(_.name))(
          equalTo(List("card6", "card3", "card4", "card5", "card1", "card2"))
        )
      }
    )
}
