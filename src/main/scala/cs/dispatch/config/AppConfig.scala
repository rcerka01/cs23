package cs.dispatch.config

import zio.ZLayer

final case class AppConfig(
    zioHttp: ZioHttpConfig,
    upstreamResponse: UpstreamResponseConfig,
    openApi: OpenApi
)
