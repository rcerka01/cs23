package cs.dispatch.domain

import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.{Codec, Encoder}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class User(name: String, creditScore: Int, salary: Int)

object User {
  given decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]
  given encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]

  given circeCodec: Codec[User] = deriveCodec
  given circeEncoder: Encoder[User] = deriveEncoder[User]
}
