package cs.dispatch.domain

import cats.data.Validated
import cats.data.NonEmptyList
import cats.implicits.*
import zio.ZIO
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class CardResponse(apr: Double, cardName: String, eligibility: Double)

class CardResponseError(msg: String) extends Exception(msg)

object CardResponse {
  implicit val decoder: JsonDecoder[CardResponse] = DeriveJsonDecoder.gen[CardResponse]
  implicit val encoder: JsonEncoder[CardResponse] = DeriveJsonEncoder.gen[CardResponse]

  private def mustHasCorrectApr(apr: Double): Validated[NonEmptyList[CardResponseError], Double] = {
    if (apr < 0) CardResponseError(s"Card apr value of $apr is smaller than 0. Item eliminated.").invalidNel
    else if (apr > 100) CardResponseError(s"Card apr value of $apr is greater than 100. Item eliminated.").invalidNel
    else apr.validNel
  }

  private def mustHaveName(name: String): Validated[NonEmptyList[CardResponseError], String] = {
    if (name.length <= 0) CardResponseError("Card name is empty. Item eliminated.").invalidNel
    else name.validNel
  }

  private def mustHasCorrectEligibility(eligibility: Double): Validated[NonEmptyList[CardResponseError], Double] = {
    if (eligibility < 0) CardResponseError(s"Card eligibility value of $eligibility is smaller than 0. Item eliminated.").invalidNel
    else if (eligibility > 10) CardResponseError(s"Card eligibility value of $eligibility is greater than 10. Item eliminated.").invalidNel
    else eligibility.validNel
  }

  def validate(cardResponse: CardResponse): Validated[NonEmptyList[CardResponseError], CardResponse] = {
    (mustHasCorrectApr(cardResponse.apr),
      mustHaveName(cardResponse.cardName),
      mustHasCorrectEligibility(cardResponse.eligibility)
    ).mapN((_, _, _) => cardResponse)
  }

//  def validate(apr: Double, cardName: String, eligibility: Double) = {
//    (mustHasCorrectApr(apr),
//      mustHaveName(cardName),
//      mustHasCorrectEligibility(eligibility)
//    ).mapN(CardResponse.apply)
//  }
}
