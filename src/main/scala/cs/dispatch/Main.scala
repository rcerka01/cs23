package cs.dispatch

import cs.dispatch.Main.Environment
import cs.dispatch.config.{AppConfig, Config, ZioHttpConfig}
import cs.dispatch.servers.HttpServer
import cs.dispatch.servers.controllers.UpstreamController
import cs.dispatch.services.UpstreamImitatorService
import zhttp.service.{ChannelFactory, EventLoopGroup, Server}
import zio.*
import zio.Console.printLine
import izumi.reflect.dottyreflection.ReflectionUtil.reflectiveUncheckedNonOverloadedSelectable

import java.io.IOException

object Main extends ZIOAppDefault {
  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = {

    def printConfig: ZIO[AppConfig, IOException, Unit] = for
      _ <- ZIO.logInfo("Starting app...")
      config <- ZIO.service[AppConfig]
      _ <- ZIO.logInfo(config.toString)
    yield ()

    printConfig
      .provide(
        Config.live
      ) *> ZIO.serviceWithZIO[HttpServer](_.create)
      .provide(
        Config.live,
        HttpServer.live,
        UpstreamImitatorService.live,
        UpstreamController.live
      )
  }
}
