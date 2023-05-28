package cs.dispatch.controllers.responses

import cats.data.{NonEmptyList, Validated}
import cats.implicits.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ScoredCardResponse(card: String, apr: Double, approvalRating: Double)

class ScoredCardResponseError(msg: String) extends Exception(msg)

object ScoredCardResponse {
  given decoder: JsonDecoder[ScoredCardResponse] = DeriveJsonDecoder.gen[ScoredCardResponse]
  given encoder: JsonEncoder[ScoredCardResponse] = DeriveJsonEncoder.gen[ScoredCardResponse]
  
  private def mustHasCorrectApr(apr: Double): Validated[NonEmptyList[ScoredCardResponseError], Double] = {
    if (apr < 0) ScoredCardResponseError(s"Scored card apr value of $apr is smaller than 0. Item eliminated.").invalidNel
    else if (apr > 100) ScoredCardResponseError(s"Scored card apr value of $apr is greater than 100. Item eliminated.").invalidNel
    else apr.validNel
  }

  private def mustHaveName(card: String): Validated[NonEmptyList[ScoredCardResponseError], String] = {
    if (card.length <= 0) ScoredCardResponseError("Scored card name is empty. Item eliminated.").invalidNel
    else card.validNel
  }

  private def mustHasCorrectApprovalRailing(approvalRating: Double): Validated[NonEmptyList[ScoredCardResponseError], Double] = {
    if (approvalRating < 0) ScoredCardResponseError(s"Scored card eligibility value of $approvalRating is smaller than 0. Item eliminated.").invalidNel
    else if (approvalRating > 1) ScoredCardResponseError(s"Scored card eligibility value of $approvalRating is greater than 1. Item eliminated.").invalidNel
    else approvalRating.validNel
  }

  def validate(scoredCardResponse: ScoredCardResponse): Validated[NonEmptyList[ScoredCardResponseError], ScoredCardResponse] = {
    (mustHasCorrectApr(scoredCardResponse.apr),
      mustHaveName(scoredCardResponse.card),
      mustHasCorrectApprovalRailing(scoredCardResponse.approvalRating)
    ).mapN((_, _, _) => scoredCardResponse)
  }
}
