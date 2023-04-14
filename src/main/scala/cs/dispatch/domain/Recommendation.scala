package cs.dispatch.domain

import cs.dispatch.domain
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Recommendation(provider: String, name: String, apr: Double, cardScore: Double)

object Recommendation {
  implicit val decoder: JsonDecoder[Recommendation] =
    DeriveJsonDecoder.gen[Recommendation]

  implicit val encoder: JsonEncoder[Recommendation] =
    DeriveJsonEncoder.gen[Recommendation]
}

