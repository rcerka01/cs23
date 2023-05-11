package cs.dispatch.domain

import io.circe.{Codec, Encoder}
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Recommendation(provider: String, name: String, apr: Double, cardScore: Double)

object Recommendation {
  
  // zio http
  given decoder: JsonDecoder[Recommendation] =
    DeriveJsonDecoder.gen[Recommendation]
  given encoder: JsonEncoder[Recommendation] =
    DeriveJsonEncoder.gen[Recommendation]

  // circe
  given circeCodec: Codec[Recommendation] = deriveCodec
  given circeEncoder: Encoder[Recommendation] = deriveEncoder[Recommendation]
  
}

