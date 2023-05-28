package cs.dispatch.services

import cs.dispatch.config
import cs.dispatch.config.{AppConfig, ConfigError}
import cs.dispatch.controllers.RecommendationController
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import zio.http.Response
import zio.{IO, RLayer, Task, URLayer, ZIO, ZLayer}
import sttp.apispec.openapi.circe.yaml.*
import java.io.BufferedWriter

trait OpenApiService {
  def generate(): Task[Unit]
}

final private case class OpenApiServiceImpl(
    appConfig: AppConfig,
    recommendationController: RecommendationController
) extends OpenApiService {

  def generate() = {
    ZIO.succeed {
      val doc: String = OpenAPIDocsInterpreter()
        .toOpenAPI(
          recommendationController.endpointsForDocs(),
          appConfig.openApi.title,
          appConfig.openApi.version
        )
        .toYaml
      val writer: BufferedWriter =
        new BufferedWriter(new java.io.FileWriter(appConfig.openApi.fileName))
      writer.write(doc)
      writer.close()
      OpenAPIDocsInterpreter()
        .toOpenAPI(
          recommendationController.endpointsForDocs(),
          appConfig.openApi.title,
          appConfig.openApi.version
        )
        .toYaml
    }
  }
}

object OpenApiService {
  lazy val live
      : RLayer[AppConfig & RecommendationController, OpenApiServiceImpl] =
    ZLayer.fromFunction(OpenApiServiceImpl.apply)
}
