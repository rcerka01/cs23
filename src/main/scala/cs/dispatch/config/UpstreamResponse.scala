package cs.dispatch.config

case class UpstreamResponse(callTypes: List[Call])

case class Call(callType: String, providerName: String, path: String, response: String)
