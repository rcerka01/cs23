package cs.dispatch.services

import cs.dispatch.Context.Env
import cs.dispatch.clients.SimpleHttpClient
import cs.dispatch.{Context, config}
import cs.dispatch.config.{AppConfig, Config, ConfigError}
import cs.dispatch.domain.{CardResponse, CreditCardResponse, Recommendation, User}
import zhttp.http.{Method, Response}
import zio.Console.printLine
import zio.json.{DeriveJsonDecoder, JsonDecoder}
import zio.{Console, ZIO, ZLayer}
import zio.json.*

class CreditCardResponseError(msg: String) extends Exception
class CardResponseError(msg: String) extends Exception

trait RecommendationService {
  def getRecommendations(user: User): ZIO[AppConfig, Throwable, List[Recommendation]]
}

case class RecommendationServiceImpl(appConfig: AppConfig)
  extends RecommendationService {

  private def callUpstream(path: String): ZIO[Env with AppConfig, Throwable, String] = {
    val port = appConfig.zioHttp.port
    val host = appConfig.zioHttp.host
    SimpleHttpClient.callHttp(port, host, path = path, Method.GET)
  }

  private def getScore(apr: Double, eligibility: Double, eligibilityWeight: Double): Double = {
    val prob = 1 / apr
    val probSq = prob * prob
    val sc = probSq * eligibility
    val scWeigh = sc / eligibilityWeight
    val rounded: Double = (scWeigh * 1000).toInt / 1000.0
    rounded
  }
  
  private def generateRecommendations(
     cards: List[CardResponse] = Nil,
     creditCards: List[CreditCardResponse] = Nil): List[Recommendation] = {

    val  cardRecommendations = cards.map(card => {
      val score = getScore(card.apr, card.eligibility, 0.1)
      Recommendation(appConfig.upstreamResponse.callTypes.head.providerName, card.cardName, card.apr, score)
    })
    val  creditCardRecommendations = creditCards.map(creditCard => {
      val score = getScore(creditCard.apr, creditCard.approvalRating, 0.01)
      Recommendation(appConfig.upstreamResponse.callTypes.tail.head.providerName, creditCard.card, creditCard.apr, score)
    })

    (cardRecommendations ++ creditCardRecommendations).sortBy(_.cardScore).reverse
  }

  def getRecommendations(user: User): ZIO[AppConfig, Throwable, List[Recommendation]] = {
      (for {
        cardResponse <- callUpstream(appConfig.upstreamResponse.callTypes.head.path)
        creditCardResponse <- callUpstream(appConfig.upstreamResponse.callTypes.tail.head.path)

        cards <- ZIO.fromEither(cardResponse.fromJson[List[CardResponse]])
          .mapError(e => CardResponseError(s"Failed serialize cards, CardResponse: $e"))
        creditCards <- ZIO.fromEither(creditCardResponse.fromJson[List[CreditCardResponse]])
          .mapError(e => CreditCardResponseError(s"Failed serialize credit cards, CreditCardResponse: $e"))

        recs <- ZIO.succeed(generateRecommendations(cards, creditCards))
      } yield (recs)).provide(Context.live, Config.live)
  }
}

object RecommendationService {
  def live: ZLayer[AppConfig, ConfigError, RecommendationServiceImpl] =
    ZLayer.fromFunction(RecommendationServiceImpl.apply)
}
