package cs.dispatch.domain

import cats.data.Validated
import cats.data.NonEmptyList
import cats.implicits.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class CreditCardResponse(card: String, apr: Double, approvalRating: Double)

class CreditCardResponseError(msg: String) extends Exception(msg)

object CreditCardResponse {
  implicit val decoder: JsonDecoder[CreditCardResponse] = DeriveJsonDecoder.gen[CreditCardResponse]
  implicit val encoder: JsonEncoder[CreditCardResponse] = DeriveJsonEncoder.gen[CreditCardResponse]


  private def mustHasCorrectApr(apr: Double): Validated[NonEmptyList[CreditCardResponseError], Double] = {
    if (apr < 0) CreditCardResponseError(s"Card apr value of $apr is smaller than 0. Item eliminated.").invalidNel
    else if (apr > 100) CreditCardResponseError(s"Card apr value of $apr is greater than 100. Item eliminated.").invalidNel
    else apr.validNel
  }

  private def mustHaveName(card: String): Validated[NonEmptyList[CreditCardResponseError], String] = {
    if (card.length <= 0) CreditCardResponseError("Card name is empty. Item eliminated.").invalidNel
    else card.validNel
  }

  private def mustHasCorrectApprovalRailing(approvalRating: Double): Validated[NonEmptyList[CreditCardResponseError], Double] = {
    if (approvalRating < 0) CreditCardResponseError(s"Card eligibility value of $approvalRating is smaller than 0. Item eliminated.").invalidNel
    else if (approvalRating > 1) CreditCardResponseError(s"Card eligibility value of $approvalRating is greater than 1. Item eliminated.").invalidNel
    else approvalRating.validNel
  }

  def validate(cardResponse: CreditCardResponse): Validated[NonEmptyList[CreditCardResponseError], CreditCardResponse] = {
    (mustHasCorrectApr(cardResponse.apr),
      mustHaveName(cardResponse.card),
      mustHasCorrectApprovalRailing(cardResponse.approvalRating)
    ).mapN((_, _, _) => cardResponse)
  }
}
