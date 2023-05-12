package cs.dispatch.servers

import cs.dispatch.clients.SimpleHttpClient
import cs.dispatch.config.AppConfig
import zio.http.Server
import zio.*
import zio.UIO
import zio.config.*
import cs.dispatch.config.Config
import cs.dispatch.Main.validateEnv
import cs.dispatch.controllers.{RecommendationController, UpstreamController}
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
    Server.serve(upstreamController.create() ++ recommendationController.create())
      .provide(
        Server.defaultWithPort(appConfig.zioHttp.port)
      )
}

object HttpServer {
  def live: RLayer[
    AppConfig & UpstreamController & RecommendationController,
    HttpServer
  ] = ZLayer.fromFunction(HttpServerImpl.apply)
}
