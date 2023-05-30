package cs.dispatch.config

import cs.dispatch.services.CallType
import zio.*

case class UpstreamResponseConfig(callTypes: List[Call])

case class Call(
    callType: CallType,
    providerName: String,
    timeout: Duration,
    response: String
)
