package cs.dispatch.services

import cs.dispatch.clients.SimpleHttpClient
import cs.dispatch.config
import cs.dispatch.config.{AppConfig, Config, ConfigError}
import zio.http.{Client, Method, Response}
import zio.Console.printLine
import zio.json.{DeriveJsonDecoder, JsonDecoder}
import zio.{Console, Duration, ZIO, ZLayer}
import zio.*
import zio.json.*
import cats.implicits.*
import cats.data.*
import cats.data.Validated.{Invalid, Valid}
import cs.dispatch.domain.{CSCardResponse, CSCardResponseError, Recommendation, ScoredCardResponse, ScoredCardResponseError, User}

trait RecommendationService {
  def getRecommendations(
      user: User
  ): ZIO[AppConfig, Throwable, List[Recommendation]]
}

final case class RecommendationServiceImpl(appConfig: AppConfig)
    extends RecommendationService {

  private def callUpstream(
      path: String,
      timeout: Duration
  ): ZIO[Client, Throwable, String] = {
    val port = appConfig.zioHttp.port
    val host = appConfig.zioHttp.host
    SimpleHttpClient.callHttp(port, host, path, Method.GET, timeout)
  }

  // todo how to make it private
  def getScore(
      apr: Double,
      eligibility: Double,
      eligibilityWeight: Double
  ): Double = {
    val prob = 1 / apr
    val probSq = prob * prob
    val sc = probSq * eligibility
    val scWeigh = sc / eligibilityWeight
    val rounded: Double = (scWeigh * 1000).toInt / 1000.0
    rounded
  }

  // todo private??
  def generateRecommendations(
      cscCardsards: List[CSCardResponse] = Nil,
      scoredCardsCards: List[ScoredCardResponse] = Nil
  ): List[Recommendation] = {

    val cscCardRecommendations = cscCardsards.map(card => {
      val score = getScore(card.apr, card.eligibility, 0.1)
      Recommendation(
        appConfig.upstreamResponse.callTypes.head.providerName,
        card.cardName,
        card.apr,
        score
      )
    })
    val creditCardRecommendations = scoredCardsCards.map(creditCard => {
      val score = getScore(creditCard.apr, creditCard.approvalRating, 0.01)
      Recommendation(
        appConfig.upstreamResponse.callTypes.tail.head.providerName,
        creditCard.card,
        creditCard.apr,
        score
      )
    })

    (cscCardRecommendations ++ creditCardRecommendations)
      .sortBy(_.cardScore)
      .reverse
  }

  def getRecommendations(
      user: User
  ): ZIO[AppConfig, Throwable, List[Recommendation]] = {
    val csCardConfig = appConfig.upstreamResponse.callTypes.head
    val scoredCardConfig = appConfig.upstreamResponse.callTypes.tail.head

    (for {
      // fibers or better with mapN ???
      cardResponseFiber <- callUpstream(
        csCardConfig.path,
        csCardConfig.timeout
      ).fork
      creditCardResponseFiber <- callUpstream(
        scoredCardConfig.path,
        scoredCardConfig.timeout
      ).fork
      zippedResponse = cardResponseFiber.zip(creditCardResponseFiber)
      response: (String, String) <- zippedResponse.join

      cscCards: List[CSCardResponse] <- response._1
        .fromJson[List[CSCardResponse]] match {
        case Right(data) => ZIO.succeed(data)
        case Left(e) =>
          ZIO.logError(
            CSCardResponseError(
              s"Failed serialize CSC Cards, CSCardResponse: $e"
            ).toString
          ) *>
            ZIO.succeed(Nil)
      }

      scoredCards <- response._2.fromJson[List[ScoredCardResponse]] match {
        case Right(data) => ZIO.succeed(data)
        case Left(e) =>
          ZIO.logError(
            ScoredCardResponseError(
              s"Failed serialize Scored Cards, ScoredCardResponse: $e"
            ).toString
          ) *>
            ZIO.succeed(Nil)
      }

      // nice, but not needed here. If one fails, all list fails.
      // validatedCardsTraverse: Validated[NonEmptyList[CardResponseError], List[CardResponse]] = cards.traverse(CardResponse.validate)

      validatedCSCardsWithExceptions: List[
        Validated[NonEmptyList[CSCardResponseError], CSCardResponse]
      ] = cscCards.map(CSCardResponse.validate)
      invalidCSCards = validatedCSCardsWithExceptions.filter(_.isInvalid)
      _ <-
        if (invalidCSCards.nonEmpty)
          ZIO.logError(s"Invalid CSC Card items: $invalidCSCards")
        else
          ZIO.logInfo(
            "Call to CSC Card upstream without errors. All items validated"
          )
      validatedCSCards: List[CSCardResponse] = validatedCSCardsWithExceptions
        .flatMap(_.toList)

      validatedScoredCardsWithExceptions: List[
        Validated[NonEmptyList[ScoredCardResponseError], ScoredCardResponse]
      ] = scoredCards.map(ScoredCardResponse.validate)
      invalidScoredCards = validatedScoredCardsWithExceptions.filter(
        _.isInvalid
      )
      _ <-
        if (invalidScoredCards.nonEmpty)
          ZIO.logError(s"Invalid Scored Card items: $invalidScoredCards")
        else
          ZIO.logInfo(
            "Call to Scored Card upstream without errors. All items validated"
          )
      validatedScoredCards: List[ScoredCardResponse] =
        validatedScoredCardsWithExceptions.flatMap(_.toList)

      recs <- ZIO.succeed(
        generateRecommendations(validatedCSCards, validatedScoredCards)
      )
    } yield (recs)).provide(
      Client.default
    )
  }
}

object RecommendationService {
  def live: ZLayer[AppConfig, ConfigError, RecommendationServiceImpl] =
    ZLayer.fromFunction(RecommendationServiceImpl.apply)
}
