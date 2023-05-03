package cs.dispatch.config

import zio.*

case class UpstreamResponseConfig(callTypes: List[Call])

case class Call(
    callType: String,
    providerName: String,
    path: String,
    timeout: Duration,
    response: String
)
