package cs.dispatch.config

import com.typesafe.config.ConfigFactory
import zio.config.*
import zio.config.magnolia.{Descriptor, descriptor}
import zio.config.typesafe.TypesafeConfig
import zio.{IO, Layer, ZIO}
import scala.concurrent.duration.{Duration, FiniteDuration}

class ConfigError(msg: String) extends Exception(msg)

val zioHttpConfigDescriptor = descriptor[ZioHttpConfig].mapKey(toKebabCase)
val upstreamResponseDescriptor = descriptor[UpstreamResponseConfig].mapKey(toKebabCase)
val openApiDescriptor = descriptor[OpenApi].mapKey(toKebabCase)
val appConfigDescriptor = descriptor[AppConfig].mapKey(toKebabCase)

object Config {
  def live: Layer[ReadError[String], AppConfig] = {
    TypesafeConfig.fromTypesafeConfig[AppConfig](ZIO.succeed(ConfigFactory.load()), appConfigDescriptor)
  }
}
