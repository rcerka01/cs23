package cs.dispatch.domain

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class CardResponse(apr: Double, cardName: String, eligibility: Double)

object CardResponse {
  implicit val decoder: JsonDecoder[CardResponse] = DeriveJsonDecoder.gen[CardResponse]
  implicit val encoder: JsonEncoder[CardResponse] = DeriveJsonEncoder.gen[CardResponse]
}
