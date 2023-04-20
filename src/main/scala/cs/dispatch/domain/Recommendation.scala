package cs.dispatch.domain

import cs.dispatch.domain
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Recommendation(provider: String, name: String, apr: Double, cardScore: Double)

object Recommendation {
  given decoder: JsonDecoder[Recommendation] =
    DeriveJsonDecoder.gen[Recommendation]

  given encoder: JsonEncoder[Recommendation] =
    DeriveJsonEncoder.gen[Recommendation]
}

