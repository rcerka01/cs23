package cs.dispatch.config

import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigValue}
import zio.config.*
import zio.config.magnolia.{Descriptor, descriptor}
import zio.config.typesafe.TypesafeConfig
import zio.{IO, Layer, ZIO}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.jdk.CollectionConverters.*

class ConfigError(msg: String) extends Exception(msg)

val zioHttpConfigDescriptor = descriptor[ZioHttpConfig].mapKey(toKebabCase)
val upstreamResponseDescriptor = descriptor[UpstreamResponseConfig].mapKey(toKebabCase)
val openApiDescriptor = descriptor[OpenApi].mapKey(toKebabCase)
val appConfigDescriptor = descriptor[AppConfig].mapKey(toKebabCase)

object Config {

  // format all config paths
  private def formatConfig(config: Config): Config = {
    def traverseConfigValue(value: ConfigValue): ConfigValue = {
      value match {
        case obj: ConfigObject =>
          val updatedObj = obj.entrySet().asScala.foldLeft(obj) { (acc, entry) =>
            val updatedValue = traverseConfigValue(entry.getValue)
            acc.withValue(entry.getKey
              .toLowerCase
              .replace("_","-"
              ), updatedValue)
          }
          updatedObj
        case _ => value
      }
    }

    val updatedRoot = config.root().entrySet().asScala.foldLeft(config.root()) { (acc, entry) =>
      val updatedValue = traverseConfigValue(entry.getValue)
      acc.withValue(entry.getKey
        .toLowerCase
        .replace("_","-"
        ), updatedValue)
    }

    ConfigFactory.parseString(updatedRoot.render())
  }

  // get config from env or fall back to application.conf
    def live: Layer[ReadError[String], AppConfig] = {
      // Load configuration from environment variables
      val envConfig: Config = formatConfig(ConfigFactory.systemEnvironment())

      // Load default configuration from application.conf
      val defaultConfig: Config = formatConfig(ConfigFactory.load())

      // Merge the environment variables configuration with the default configuration
      val conf: Config = envConfig.withFallback(defaultConfig)

      TypesafeConfig.fromTypesafeConfig[AppConfig](ZIO.succeed(conf), appConfigDescriptor)
    }
}
