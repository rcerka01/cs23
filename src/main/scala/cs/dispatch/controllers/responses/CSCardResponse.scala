package cs.dispatch.controllers.responses

import cats.data.{NonEmptyList, Validated}
import cats.implicits.*
import zio.ZIO
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class CSCardResponse(apr: Double, cardName: String, eligibility: Double)

class CSCardResponseError(msg: String) extends Exception(msg)

object CSCardResponse {
  given decoder: JsonDecoder[CSCardResponse] = DeriveJsonDecoder.gen[CSCardResponse]
  given encoder: JsonEncoder[CSCardResponse] = DeriveJsonEncoder.gen[CSCardResponse]

  private def mustHasCorrectApr(apr: Double): Validated[NonEmptyList[CSCardResponseError], Double] = {
    if (apr < 0) CSCardResponseError(s"CS Card apr value of $apr is smaller than 0. Item eliminated.").invalidNel
    else if (apr > 100) CSCardResponseError(s"CS Card apr value of $apr is greater than 100. Item eliminated.").invalidNel
    else apr.validNel
  }

  private def mustHaveName(name: String): Validated[NonEmptyList[CSCardResponseError], String] = {
    if (name.length <= 0) CSCardResponseError("CS Card name is empty. Item eliminated.").invalidNel
    else name.validNel
  }

  private def mustHasCorrectEligibility(eligibility: Double): Validated[NonEmptyList[CSCardResponseError], Double] = {
    if (eligibility < 0) CSCardResponseError(s"CS Card eligibility value of $eligibility is smaller than 0. Item eliminated.").invalidNel
    else if (eligibility > 10) CSCardResponseError(s"CS Card eligibility value of $eligibility is greater than 10. Item eliminated.").invalidNel
    else eligibility.validNel
  }

  def validate(cscCardResponse: CSCardResponse): Validated[NonEmptyList[CSCardResponseError], CSCardResponse] = {
    (mustHasCorrectApr(cscCardResponse.apr),
      mustHaveName(cscCardResponse.cardName),
      mustHasCorrectEligibility(cscCardResponse.eligibility)
    ).mapN((_, _, _) => cscCardResponse)
  }

//  def validate(apr: Double, cardName: String, eligibility: Double) = {
//    (mustHasCorrectApr(apr),
//      mustHaveName(cardName),
//      mustHasCorrectEligibility(eligibility)
//    ).mapN(CardResponse.apply)
//  }
}
