package cs.dispatch.servers

import cs.dispatch.Context
import cs.dispatch.clients.SimpleHttpClient
import cs.dispatch.config.AppConfig
import cs.dispatch.servers.controllers.{
  RecommendationController,
  UpstreamController
}
import zhttp.http.*
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{
  Channel,
  ChannelFactory,
  EventLoopGroup,
  Server,
  ServerChannelFactory
}
import zio.*
import zio.UIO
import zio.config.*
import cs.dispatch.config.Config
import cs.dispatch.Main.validateEnv
import cs.dispatch.services.{RecommendationService, UpstreamImitatorService}

trait HttpServer {
  def create: Task[ExitCode]
}

case class HttpServerImpl(
    appConfig: AppConfig,
    upstreamController: UpstreamController,
    recommendationController: RecommendationController
) extends HttpServer {
  override def create: Task[ExitCode] =
    Server(upstreamController.create() ++ recommendationController.create())
      .withPort(appConfig.zioHttp.port)
      .start
      .provide(
        Config.live,
        Context.live,
        UpstreamImitatorService.live,
        RecommendationService.live
      )
}

object HttpServer {
  def live: RLayer[
    AppConfig & UpstreamController & RecommendationController,
    HttpServer
  ] =
    ZLayer.fromFunction(HttpServerImpl.apply)
}
