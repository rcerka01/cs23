package cs.dispatch.services

import cs.dispatch.config
import cs.dispatch.config.{AppConfig, ConfigError}
import zio.http.Response
import zio.{IO, RLayer, URLayer, ZIO, ZLayer}

enum CallType:
  case CreditCards, Cards

trait UpstreamImitatorService {
  def cardImitator(callType: CallType): IO[ConfigError, String]
}
import cs.dispatch.Main.validateEnv

final private case class UpstreamImitatorServiceImpl(appConfig: AppConfig)
  extends UpstreamImitatorService {

  def cardImitator(callType: CallType): IO[ConfigError, String] = {
    appConfig.upstreamResponse.callTypes.find(_.callType.equals(callType)) match {
      case Some(config) => ZIO.succeed(config.response.stripMargin)
      case _ =>
        ZIO.logError("Service failed due missing config") *>
        ZIO.fail(new ConfigError("Config for this call type not exists"))
    }
  }
}

object UpstreamImitatorService {
  lazy val live: RLayer[AppConfig, UpstreamImitatorServiceImpl] =
    ZLayer.fromFunction(UpstreamImitatorServiceImpl.apply)
}
