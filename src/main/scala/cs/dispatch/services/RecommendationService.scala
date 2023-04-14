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
  
  // todo
  private def generateRecommendations(
     cards: List[CardResponse] = Nil,
     creditCards: List[CreditCardResponse] = Nil): List[Recommendation] = {
      println("xxxxx")
      println("xxxxx" + cards)
      println("xxxxx" + creditCards)
      println("xxxxx")
      List(Recommendation("end","of world", 1, 2))
  }

  def getRecommendations(user: User): ZIO[AppConfig, Throwable, List[Recommendation]] = {
      (for {
        cardResponse <- callUpstream("/app.clearscore.com/api/global/backend-tech-test/v1/cards")
        creditCardResponse <- callUpstream("/app.clearscore.com/api/global/backend-tech-test/v2/creditcards")

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
