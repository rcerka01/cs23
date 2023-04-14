package cs.dispatch.domain

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class CreditCardResponse(card: String, apr: Double, approvalRating: Double)

object CreditCardResponse {
  implicit val decoder: JsonDecoder[CreditCardResponse] = DeriveJsonDecoder.gen[CreditCardResponse]
  implicit val encoder: JsonEncoder[CreditCardResponse] = DeriveJsonEncoder.gen[CreditCardResponse]
}
