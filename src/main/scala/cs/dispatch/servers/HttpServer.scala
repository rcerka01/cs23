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
import cs.dispatch.services.{OpenApiService, RecommendationService, UpstreamImitatorService}
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import java.io.{BufferedWriter, File}
import sttp.apispec.openapi.circe.yaml.*

trait HttpServer {
  def create: Task[ExitCode]
}

case class HttpServerImpl(
    appConfig: AppConfig,
    upstreamController: UpstreamController,
    recommendationController: RecommendationController,
    openApiService: OpenApiService
) extends HttpServer {
  override def create: Task[ExitCode] =
    openApiService.generate() *>
    Server
      .serve(upstreamController.create() ++ recommendationController.create())
      .provide(
        Server.defaultWithPort(appConfig.zioHttp.port)
      )
}

object HttpServer {
  lazy val live: RLayer[
    AppConfig & UpstreamController & RecommendationController & OpenApiService,
    HttpServer
  ] = ZLayer.fromFunction(HttpServerImpl.apply)
}
