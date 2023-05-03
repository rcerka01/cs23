package cs.dispatch

import cs.dispatch.config.{AppConfig, Config}
import cs.dispatch.services.UpstreamImitatorService
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{ChannelFactory, EventLoopGroup}

object Context {
  type Env = EventLoopGroup & ChannelFactory
  val live =
    EventLoopGroup.auto(0) ++ ServerChannelFactory.auto ++ ChannelFactory.auto
}
