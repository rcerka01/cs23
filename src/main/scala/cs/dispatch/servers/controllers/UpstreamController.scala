package cs.dispatch.servers.controllers

import cs.dispatch.Context.*
import cs.dispatch.clients.SimpleHttpClient
import zhttp.http.{HttpApp, *}
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.*
import zio.UIO
import zio.config.*
import cs.dispatch.Main.validateEnv
import cs.dispatch.config.{AppConfig, ConfigError}
import cs.dispatch.domain.HttpErrorResponses.toHttpError
import cs.dispatch.services.{CallType, UpstreamImitatorService}

trait UpstreamController {
  def create(): HttpApp[Env & UpstreamImitatorService & AppConfig, Throwable]
}

case class UpstreamControllerImpl(upstreamService: UpstreamImitatorService)
    extends UpstreamController {
  private type UpstreamControllerEnv = Env & UpstreamImitatorService & AppConfig

  private val upstreamApp: HttpApp[UpstreamControllerEnv, Throwable] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "test" =>
        req.bodyAsString.map(body => Response.json(body))
      case req @ Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v1" / "cards" =>
        upstreamService
          .cardImitator(CallType.Cards)
          .fold(e => Response.fromHttpError(toHttpError(e)), Response.json)
          .delay(1.seconds)
      case req @ Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v2" / "creditcards" =>
        upstreamService
          .cardImitator(CallType.CreditCards)
          .fold(e => Response.fromHttpError(toHttpError(e)), Response.json)
          .delay(1.seconds)
    }

  override def create(): HttpApp[UpstreamControllerEnv, Throwable] = upstreamApp
}
object UpstreamController {
  def live: RLayer[UpstreamImitatorService, UpstreamController] =
    ZLayer.fromFunction(UpstreamControllerImpl.apply)
}
