package cs.dispatch.config

import com.typesafe.config.ConfigFactory
import zio.config.*
import zio.config.magnolia.{Descriptor, descriptor}
import zio.config.typesafe.TypesafeConfig
import zio.{IO, Layer, ZIO}
import scala.concurrent.duration.{Duration, FiniteDuration}

class ConfigError(msg: String) extends Exception

val zioHttpConfigDescriptor = descriptor[ZioHttpConfig].mapKey(toKebabCase)
val upstreamResponseDescriptor = descriptor[UpstreamResponse].mapKey(toKebabCase)
val appConfigDescriptor = descriptor[AppConfig].mapKey(toKebabCase)

object Config {
  def live: Layer[ReadError[String], AppConfig] = {
    TypesafeConfig.fromTypesafeConfig[AppConfig](ZIO.succeed(ConfigFactory.load()), appConfigDescriptor)
  }
}
