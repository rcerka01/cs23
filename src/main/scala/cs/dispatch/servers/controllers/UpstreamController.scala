package cs.dispatch.servers.controllers

import cs.dispatch.Context.*
import cs.dispatch.clients.SimpleHttpClient
import zhttp.http.{HttpApp, *}
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.*
import zio.UIO
import zio.config.*
import cs.dispatch.Main.validateEnv
import cs.dispatch.config.AppConfig
import cs.dispatch.services.{CallType, UpstreamImitatorService}

trait UpstreamController {
  def create(): HttpApp[UpstreatControllerEnv, Throwable]
}

case class UpstreamControllerImpl(upstreamService: UpstreamImitatorService) extends UpstreamController {
   private type UpstreatControllerEnv = Env & UpstreamImitatorService & AppConfig

  private val upstreamApp: HttpApp[UpstreatControllerEnv, Throwable]  =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "test" =>
        req.bodyAsString.map(body => Response.json(body))
      case req@Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v1" / "cards" =>
        upstreamService.cardImitator(CallType.Cards).map(Response.json)
      case req@Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v2" / "creditcards" =>
        upstreamService.cardImitator(CallType.CreditCards).map(Response.json)
    }

  override def create(): HttpApp[UpstreatControllerEnv, Throwable]  = upstreamApp
}

object UpstreamController {
  def live: RLayer[UpstreamImitatorService, UpstreamController] =
    ZLayer.fromFunction(UpstreamControllerImpl.apply)
}
