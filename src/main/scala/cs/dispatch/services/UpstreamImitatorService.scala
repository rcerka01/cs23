package cs.dispatch.services

import cs.dispatch.config
import cs.dispatch.config.{AppConfig, ConfigError}
import zio.http.Response
import zio.{ZIO, ZLayer}

enum CallType:
  case CreditCards, Cards

trait UpstreamImitatorService {
  def cardImitator(callType: CallType): ZIO[AppConfig, ConfigError, String]
}

case class UpstreamImitatorServiceImpl(appConfig: AppConfig)
  extends UpstreamImitatorService {

  def cardImitator(callType: CallType): ZIO[Any, ConfigError, String] = {
    appConfig.upstreamResponse.callTypes.find(_.callType.equals(callType.toString.toLowerCase())) match {
      case Some(config) => ZIO.succeed(config.response.stripMargin)
      case _ =>
        ZIO.logError("Service failed due missing config") *>
        ZIO.fail(new ConfigError("Config for this call type not exists"))
    }
  }
}

object UpstreamImitatorService {
  def live: ZLayer[AppConfig, ConfigError, UpstreamImitatorServiceImpl] =
    ZLayer.fromFunction(UpstreamImitatorServiceImpl.apply)
}
